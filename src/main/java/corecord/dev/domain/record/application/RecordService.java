package corecord.dev.domain.record.application;

import corecord.dev.domain.record.domain.dto.request.RecordRequest;
import corecord.dev.domain.record.domain.dto.response.RecordResponse;


public interface RecordService {

    // record
    RecordResponse.MemoRecordDto createMemoRecord(Long userId, RecordRequest.RecordDto recordDto);
    RecordResponse.MemoRecordDto getMemoRecordDetail(Long userId, Long recordId);

    // tmp record
    void createTmpMemoRecord(Long userId, RecordRequest.TmpMemoRecordDto tmpMemoRecordDto);
    RecordResponse.TmpMemoRecordDto getTmpMemoRecord(Long userId);

    // record list
    RecordResponse.RecordListDto getRecordListByFolder(Long userId, String folderName, Long lastRecordId);
    RecordResponse.RecordListDto getRecordListByKeyword(Long userId, String keywordValue, Long lastRecordId);
    RecordResponse.RecordListDto getRecentRecordList(Long userId);

    void updateFolderOfRecord(Long userId, RecordRequest.UpdateFolderDto updateFolderDto);

}
