package com.mmc.bookduck.domain.folder.service;

import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.book.service.UserBookService;
import com.mmc.bookduck.domain.folder.dto.common.FolderBookCoverListDto;
import com.mmc.bookduck.domain.folder.dto.request.FolderRequestDto;
import com.mmc.bookduck.domain.folder.dto.response.AllFolderListResponseDto;
import com.mmc.bookduck.domain.folder.dto.response.FolderBookListResponseDto;
import com.mmc.bookduck.domain.folder.dto.common.FolderBookUnitDto;
import com.mmc.bookduck.domain.folder.dto.response.FolderResponseDto;
import com.mmc.bookduck.domain.folder.entity.Folder;
import com.mmc.bookduck.domain.folder.entity.FolderBook;
import com.mmc.bookduck.domain.folder.repository.FolderRepository;
import com.mmc.bookduck.domain.user.entity.User;
import com.mmc.bookduck.domain.user.repository.UserRepository;
import com.mmc.bookduck.domain.user.service.UserService;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserBookService userBookService;
    private final FolderBookService folderBookService;
    private final UserService userService;


    // 폴더 생성
    public FolderResponseDto createFolder(FolderRequestDto dto) {
        User user = userService.getCurrentUser();
        Folder folder = new Folder(dto.folderName(), user);

        Folder savedFolder = folderRepository.save(folder);
        return FolderResponseDto.from(savedFolder);
    }

    // 폴더명 수정
    public FolderResponseDto updateFolder(Long folderId, FolderRequestDto dto) {
        User user = userService.getCurrentUser();
        Folder folder = findFolderById(folderId);

        folder.updateFolderName(dto.folderName());
        return FolderResponseDto.from(folder);
    }

    // 폴더 삭제
    public String deleteFolder(Long folderId) {
        User user = userService.getCurrentUser();
        Folder folder = findFolderById(folderId);

        // 권한확인
        if(folder.getUser().equals(user)){
            // 폴더 책들을 다 삭제
            folderBookService.deleteFolderBookList(folder);
            folderRepository.delete(folder);
            return "폴더를 삭제했습니다.";
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }


    // 폴더에 책 추가
    public FolderBookListResponseDto addFolderBook(Long folderId, Long userBookId) {
        User user = userService.getCurrentUser();

        Folder folder = findFolderById(folderId);
        UserBook userBook = userBookService.findUserBookById(userBookId);

        List<FolderBookUnitDto> folderBookList = new ArrayList<>();

        // 권한확인
        if(folder.getUser().equals(user) && userBook.getUser().equals(user)){
            if(folderBookService.existsByUserBookAndFolder(userBook, folder)){
                // 이미 folderBook 있을 때,
                throw new CustomException(ErrorCode.FOLDERBOOK_ALREADY_EXISTS);
            }
            FolderBook newFolderBook = folderBookService.createFolderBook(userBook, folder);
            folder.addFolderBook(newFolderBook);

            for(FolderBook folderBook : folder.getFolderBooks()){
                folderBookList.add(FolderBookUnitDto.from(folderBook));
            }
            return new FolderBookListResponseDto(folder, folderBookList);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }

    // 폴더에서 책 삭제
    public FolderBookListResponseDto deleteFolderBook(Long folderId, Long folderBookId) {
        User user = userService.getCurrentUser();
        Folder folder = findFolderById(folderId);
        FolderBook folderBook = folderBookService.findFolderBookById(folderBookId);

        List<FolderBookUnitDto> folderBookList = new ArrayList<>();

        // 권한확인
        if(folder.getUser().equals(user) && folderBook.getUserBook().getUser().equals(user)){
            folder.removeFolderBook(folderBook);
            folderBookService.deleteOneFolderBook(folderBook);

            for(FolderBook book : folder.getFolderBooks()){
                folderBookList.add(FolderBookUnitDto.from(book));
            }
            return new FolderBookListResponseDto(folder, folderBookList);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }

    // 폴더 별 도서 목록 조회
    public FolderBookListResponseDto getFolderBookList(Long folderId) {
        User user = userService.getCurrentUser();
        Folder folder = findFolderById(folderId);

        List<FolderBookUnitDto> folderBookList = new ArrayList<>();

        if(folder.getUser().equals(user)){
            for(FolderBook folderBook : folder.getFolderBooks()){
                folderBookList.add(FolderBookUnitDto.from(folderBook));
            }
            return new FolderBookListResponseDto(folder, folderBookList);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }

    }

    // 전체 폴더 목록 조회
    public AllFolderListResponseDto getAllFolderList() {
        User user = userService.getCurrentUser();
        List<Folder> folders = folderRepository.findAllByUser(user);

        List<FolderBookCoverListDto> folderList = new ArrayList<>();

        if(folders != null){
            for(Folder folder : folders){
                folderList.add(FolderBookCoverListDto.from(folder, folder.getFolderBooks()));
            }
        }
        return new AllFolderListResponseDto(folderList);
    }

    public Folder findFolderById(Long folderId){
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(()-> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
        return folder;
    }

    // 폴더별 & 상태별 도서 목록 조회
    public FolderBookListResponseDto getFolderBookListStatus(Long folderId, String status) {
        User user = userService.getCurrentUser();
        Folder folder = findFolderById(folderId);

        List<FolderBookUnitDto> folderBookList = new ArrayList<>();

        if(folder.getUser().equals(user)){
            for(FolderBook folderBook : folder.getFolderBooks()){
                if(folderBook.getUserBook().getReadStatus().name().equals(status)){
                    folderBookList.add(FolderBookUnitDto.from(folderBook));
                }
            }
            return new FolderBookListResponseDto(folder, folderBookList);
        }else{
            throw new CustomException(ErrorCode.UNAUTHORIZED_REQUEST);
        }
    }


    /*
    public CandidateFolderBookListResponseDto getCandidateBooks(Long folderId) {
        Folder folder = findFolderById(folderId);

        //folder
        //return new CandidateFolderBookListResponseDto(folderId, coverList);
    }
    */
}
