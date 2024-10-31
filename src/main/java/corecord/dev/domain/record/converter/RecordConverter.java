package corecord.dev.domain.record.converter;

import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.analysis.entity.Ability;
import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.record.constant.RecordType;
import corecord.dev.domain.record.dto.response.RecordResponse;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.user.entity.User;

import java.util.List;

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

    public static RecordResponse.TmpMemoRecordDto toExistingTmpMemoRecordDto(Record record) {
        return RecordResponse.TmpMemoRecordDto.builder()
                .isExist(true)
                .title(record.getTitle())
                .content(record.getContent())
                .build();
    }

    public static RecordResponse.TmpMemoRecordDto toNotExistingTmpMemoRecordDto() {
        return RecordResponse.TmpMemoRecordDto.builder()
                .isExist(false)
                .title(null)
                .content(null)
                .build();
    }

    public static RecordResponse.RecordDto toRecordDto(Record record) {
        List<String> keywordList = record.getAnalysis().getAbilityList().stream()
                .map(Ability::getKeyword)
                .map(Keyword::getValue)
                .toList();

        return RecordResponse.RecordDto.builder()
                .recordId(record.getRecordId())
                .title(record.getTitle())
                .keywordList(keywordList)
                .createdAt(record.getCreatedAtFormatted())
                .build();
    }

    public static RecordResponse.RecordListDto toRecordListDto(String folder, List<Record> recordList) {
        List<RecordResponse.RecordDto> recordDtoList = recordList.stream()
                .map(RecordConverter::toRecordDto)
                .toList();

        return RecordResponse.RecordListDto.builder()
                .folder(folder)
                .recordDtoList(recordDtoList)
                .build();
    }
}
