package com.mmc.bookduck.domain.folder.entity;

import com.mmc.bookduck.domain.book.entity.UserBook;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FolderBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long folderBookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", updatable = false)
    @NotNull
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_book_id", updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE) // 다대일 단방향이므로 설정
    private UserBook userBook;

    @Builder
    public FolderBook(UserBook userBook, Folder folder) {
        this.folder = folder;
        this.userBook = userBook;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
