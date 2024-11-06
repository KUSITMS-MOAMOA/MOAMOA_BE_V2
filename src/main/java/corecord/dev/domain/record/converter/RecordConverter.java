package corecord.dev.domain.record.converter;

import corecord.dev.domain.analysis.constant.Keyword;
import corecord.dev.domain.analysis.entity.Ability;
import corecord.dev.domain.chat.entity.ChatRoom;
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
                .type(RecordType.MEMO)
                .build();
    }

    public static Record toChatRecordEntity(String title, String content, User user, Folder folder, ChatRoom chatRoom) {
        return Record.builder()
                .type(RecordType.CHAT)
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
                .folder(record.getFolder().getTitle())
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

    public static RecordResponse.KeywordRecordDto toKeywordRecordDto(Record record) {
        String content = record.getContent();
        String truncatedContent = content.length() > 30 ? content.substring(0, 30) : content;

        return RecordResponse.KeywordRecordDto.builder()
                .analysisId(record.getAnalysis().getAnalysisId())
                .folder(record.getFolder().getTitle())
                .title(record.getTitle())
                .content(truncatedContent)
                .createdAt(record.getCreatedAtFormatted())
                .build();
    }

    public static RecordResponse.KeywordRecordListDto toKeywordRecordListDto(List<Record> recordList) {
        List<RecordResponse.KeywordRecordDto> keywordRecordDtoList = recordList.stream()
                .map(RecordConverter::toKeywordRecordDto)
                .toList();

        return RecordResponse.KeywordRecordListDto.builder()
                .recordDtoList(keywordRecordDtoList)
                .build();
    }
}
