package corecord.dev.domain.record.application;

import corecord.dev.domain.chat.domain.entity.ChatRoom;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.dto.request.RecordRequest;
import corecord.dev.domain.record.domain.dto.response.RecordResponse;
import corecord.dev.domain.user.domain.entity.User;


public interface RecordService {

    // record
    RecordResponse.RecordAnalysisDto createRecord(Long userId, RecordRequest.RecordDto recordDto);
    RecordResponse.MemoRecordDto getMemoRecordDetail(Long userId, Long recordId);

    // tmp record
    void createTmpMemoRecord(Long userId, RecordRequest.TmpMemoRecordDto tmpMemoRecordDto);
    RecordResponse.TmpMemoRecordDto getTmpMemoRecord(Long userId);
    void createExampleRecord(User user, Folder folder, ChatRoom chatRoom);

    // record list
    RecordResponse.RecordListDto getRecordListByFolder(Long userId, String folderName, Long lastRecordId);
    RecordResponse.RecordListDto getRecordListByKeyword(Long userId, String keywordValue, Long lastRecordId);
    RecordResponse.RecordListDto getRecentRecordList(Long userId);

    void updateFolderOfRecord(Long userId, RecordRequest.UpdateFolderDto updateFolderDto);

}
