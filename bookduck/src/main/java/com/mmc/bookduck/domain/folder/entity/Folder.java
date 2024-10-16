package com.mmc.bookduck.domain.folder.entity;

import com.mmc.bookduck.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long folderId;

    @NotNull
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE) // 다대일 단방향이므로 설정
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderBook> folderBooks;

    @Builder
    public Folder(String folderName, User user) {
        this.folderName = folderName;
        this.user = user;
        this.folderBooks = new ArrayList<>();
    }
    // folder 이름 수정
    public void updateFolderName(String folderName) { this.folderName = folderName;}

    // folderBook 추가
    public void addFolderBook(FolderBook folderBook) {
        this.folderBooks.add(folderBook);
    }

    // folderBook 삭제
    public void removeFolderBook(FolderBook folderBook) {
        folderBooks.remove(folderBook);
        folderBook.setFolder(null);
    }
}