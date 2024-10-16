package com.mmc.bookduck.domain.folder.service;

import com.mmc.bookduck.domain.book.entity.UserBook;
import com.mmc.bookduck.domain.folder.entity.Folder;
import com.mmc.bookduck.domain.folder.entity.FolderBook;
import com.mmc.bookduck.domain.folder.repository.FolderBookRepository;
import com.mmc.bookduck.global.exception.CustomException;
import com.mmc.bookduck.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderBookService {
    private final FolderBookRepository folderBookRepository;

    // folderBook 생성
    public FolderBook createFolderBook(UserBook userBook, Folder folder){
        FolderBook folderBook = new FolderBook(userBook, folder);
        return folderBookRepository.save(folderBook);
    }

    // 폴더삭제 시 folderBook 모두 삭제
    public void deleteFolderBookList(Folder folder){
        List<FolderBook> folderBookList = folder.getFolderBooks();
        folderBookRepository.deleteAll(folderBookList);
    }

    // 폴더에서 책 삭제
    public void deleteOneFolderBook(FolderBook folderBook){
        folderBookRepository.delete(folderBook);
    }

    public boolean existsByUserBookAndFolder(UserBook userBook, Folder folder) {
        return folderBookRepository.existsByUserBookAndFolder(userBook,folder);
    }

    public FolderBook findFolderBookById(Long folderBookId) {
        FolderBook folderBook = folderBookRepository.findById(folderBookId)
                .orElseThrow(()->new CustomException(ErrorCode.FOLDERBOOK_NOT_FOUND));
        return folderBook;
    }
}

