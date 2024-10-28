package corecord.dev.domain.record.converter;

import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.record.constant.RecordType;
import corecord.dev.domain.record.dto.response.RecordResponse;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.user.entity.User;

public class RecordConverter {
    public static Record toMemoRecordEntity(String title, String content, User user, Folder folder) {
        return Record.builder()
                .type(RecordType.MEMO)
                .title(title)
                .user(user)
                .content(content)
                .folder(folder)
                .build();
    }

    public static RecordResponse.MemoRecordDto toMemoRecordDto(Record record) {
        return RecordResponse.MemoRecordDto.builder()
                .recordId(record.getRecordId())
                .title(record.getTitle())
                .content(record.getContent())
                .folder(record.getFolder().getTitle())
                .createdAt(record.getCreatedAtFormatted())
                .build();
    }
}
