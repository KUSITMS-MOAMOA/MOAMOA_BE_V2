package corecord.dev.domain.record.application;

import corecord.dev.domain.ability.domain.enums.Keyword;
import corecord.dev.domain.folder.domain.entity.Folder;
import corecord.dev.domain.record.domain.entity.Record;
import corecord.dev.domain.record.domain.repository.RecordRepository;
import corecord.dev.domain.record.exception.RecordException;
import corecord.dev.domain.record.status.RecordErrorStatus;
import corecord.dev.domain.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordDbService {
    private final RecordRepository recordRepository;

    private final int listSize = 30;

    @Transactional
    public Record saveRecord(Record record) {
        return recordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Record record) {
        recordRepository.delete(record);
    }

    @Transactional
    public void deleteRecordByUserId(Long userId) {
        recordRepository.deleteRecordByUserId(userId);
    }

    @Transactional
    public void deleteRecordByFolder(Folder folder) {
        recordRepository.deleteRecordByFolder(folder);
    }

    public int getRecordCount(Long userId) {
        return recordRepository.getRecordCount(userId);
    }

    public int getRecordCountByChatType(Long userId) {
        return recordRepository.getRecordCountByType(userId);
    }

    @Transactional
    public void updateRecordTitle(Record record, String title) {
        record.updateTitle(title);
    }

    public Record findRecordById(Long recordId) {
        return recordRepository.findRecordById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorStatus.RECORD_NOT_FOUND));
    }

    public Record findTmpRecordById(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordException(RecordErrorStatus.RECORD_NOT_FOUND));
    }

    public List<Record> findRecordListByFolder(Long userId, Folder folder, Long lastRecordId) {
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecordsByFolder(folder, userId, lastRecordId, pageable);
    }

    public List<Record> findRecordList(Long userId, Long lastRecordId) {
        // -1일 경우 최근 생성된 6개 리스트만 조회
        int newListSize = lastRecordId == -1 ? 6 : listSize + 1;

        Pageable pageable = PageRequest.of(0, newListSize, Sort.by("createdAt").descending());
        return recordRepository.findRecords(userId, lastRecordId, pageable);
    }

    public List<Record> findRecordListByKeyword(Long userId, Keyword keyword, Long lastRecordId) {
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecordsByKeyword(keyword, userId, lastRecordId, pageable);
    }
}
