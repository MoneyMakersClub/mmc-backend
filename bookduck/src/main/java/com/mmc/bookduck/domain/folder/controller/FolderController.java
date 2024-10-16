package com.mmc.bookduck.domain.folder.controller;

import com.mmc.bookduck.domain.folder.dto.request.FolderRequestDto;
import com.mmc.bookduck.domain.folder.dto.response.AllFolderListResponseDto;
import com.mmc.bookduck.domain.folder.dto.response.FolderBookListResponseDto;
import com.mmc.bookduck.domain.folder.dto.response.FolderResponseDto;
import com.mmc.bookduck.domain.folder.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {
    private final FolderService folderService;

    // 폴더생성
    @PostMapping
    public ResponseEntity<FolderResponseDto> createFolder(@RequestBody FolderRequestDto dto){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(folderService.createFolder(dto));
    }

    //폴더 수정
    @PatchMapping("/{folderId}")
    public ResponseEntity<FolderResponseDto> updateFolder(@PathVariable final Long folderId,
                                                          @RequestBody FolderRequestDto dto){

        return ResponseEntity.status(HttpStatus.OK)
                .body(folderService.updateFolder(folderId, dto));
    }

    // 폴더 삭제
    @DeleteMapping("/{folderId}")
    public ResponseEntity<String> deleteFolder(@PathVariable(name = "folderId") final Long folderId){

        return ResponseEntity.status(HttpStatus.OK)
                .body(folderService.deleteFolder(folderId));
    }

    // 폴더에 책 추가
    @PostMapping("/{folderId}/books/{userbookId}")
    public ResponseEntity<FolderBookListResponseDto> addFolderBook(@PathVariable(name="folderId") final Long folderId,
                                                                   @PathVariable(name="userbookId") final Long userBookId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(folderService.addFolderBook(folderId, userBookId));
    }

    /*
    // 폴더에 추가할 후보 책 리스트 조회
    @GetMapping("/{folderId}/books/candidates")
    public ResponseEntity<CandidateFolderBookListResponseDto> getCandidateBooks(@PathVariable(name="folderId") final Long folderId){

        return ResponseEntity.status(HttpStatus.OK)
                .body(folderService.getCandidateBooks(folderId));
    }
    */


    //폴더에서 책 삭제
    @DeleteMapping("/{folderId}/books/{folderbookId}")
    public ResponseEntity<FolderBookListResponseDto> deleteFolderBook(@PathVariable(name="folderId") final Long folderId,
                                                                      @PathVariable(name="folderbookId") final Long folderBookId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(folderService.deleteFolderBook(folderId, folderBookId));
    }

    //폴더 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<AllFolderListResponseDto> getAllFolderList(){

        return ResponseEntity.status(HttpStatus.OK)
                .body(folderService.getAllFolderList());
    }

    //폴더별 책 목록 조회
    @GetMapping("/{folderId}/books")
    public ResponseEntity<FolderBookListResponseDto> getFolderBookList(@PathVariable(name="folderId") final Long folderId){

        return ResponseEntity.status(HttpStatus.OK)
                .body((folderService.getFolderBookList(folderId)));
    }

    //폴더별&상태별 책 목록 조회
    @GetMapping("/{folderId}/books/filter")
    public ResponseEntity<FolderBookListResponseDto> getFolderBookListStatus(@PathVariable(name="folderId") final Long folderId,
                                                                       @RequestParam(name="status") final String status){

        return ResponseEntity.status(HttpStatus.OK)
                .body((folderService.getFolderBookListStatus(folderId, status)));
    }

}
