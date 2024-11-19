package corecord.dev.domain.record.domain.converter;

import corecord.dev.domain.ability.domain.entity.Keyword;
import corecord.dev.domain.ability.domain.entity.Ability;
import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.entity.RecordType;
import corecord.dev.domain.record.domain.dto.response.RecordResponse;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.user.domain.entity.User;

import java.util.List;

public class RecordConverter {
    public static Record toMemoRecordEntity(String title, String content, User user, Folder folder) {
        return Record.builder()
                .title(title)
                .user(user)
                .content(content)
                .folder(folder)
                .type(RecordType.MEMO)
                .build();
    }

    public static Record toChatRecordEntity(String title, String content, User user, Folder folder, ChatRoom chatRoom) {
        return Record.builder()
                .title(title)
                .user(user)
                .content(content)
                .folder(folder)
                .chatRoom(chatRoom)
                .type(RecordType.CHAT)
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
                .analysisId(record.getAnalysis().getAnalysisId())
                .recordId(record.getRecordId())
                .folder(record.getFolder().getTitle())
                .title(record.getTitle())
                .keywordList(keywordList)
                .createdAt(record.getCreatedAtFormatted())
                .build();
    }

    public static RecordResponse.RecordListDto toRecordListDto(String folder, List<Record> recordList, boolean hasNext) {
        List<RecordResponse.RecordDto> recordDtoList = recordList.stream()
                .map(RecordConverter::toRecordDto)
                .toList();

        return RecordResponse.RecordListDto.builder()
                .folder(folder)
                .recordDtoList(recordDtoList)
                .hasNext(hasNext)
                .build();
    }

    public static RecordResponse.KeywordRecordDto toKeywordRecordDto(Record record) {
        String content = record.getContent();
        String truncatedContent = content.length() > 30 ? content.substring(0, 30) : content;

        return RecordResponse.KeywordRecordDto.builder()
                .analysisId(record.getAnalysis().getAnalysisId())
                .recordId(record.getRecordId())
                .folder(record.getFolder().getTitle())
                .title(record.getTitle())
                .content(truncatedContent)
                .createdAt(record.getCreatedAtFormatted())
                .build();
    }

    public static RecordResponse.KeywordRecordListDto toKeywordRecordListDto(List<Record> recordList, boolean hasNext) {
        List<RecordResponse.KeywordRecordDto> keywordRecordDtoList = recordList.stream()
                .map(RecordConverter::toKeywordRecordDto)
                .toList();

        return RecordResponse.KeywordRecordListDto.builder()
                .recordDtoList(keywordRecordDtoList)
                .hasNext(hasNext)
                .build();
    }
}
