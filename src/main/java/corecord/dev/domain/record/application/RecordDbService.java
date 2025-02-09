package corecord.dev.domain.record.application;

import corecord.dev.domain.ability.domain.entity.Keyword;
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

    public int getRecordCount(User user) {
        return recordRepository.getRecordCount(user);
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
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecords(userId, lastRecordId, pageable);
    }

    public List<Record> findRecordListOrderByCreatedAt(Long userId) {
        Pageable pageable = PageRequest.of(0, 6, Sort.by("createdAt").descending());
        return recordRepository.findRecordsOrderByCreatedAt(userId, pageable);
    }

    public List<Record> findRecordListByKeyword(Long userId, Keyword keyword, Long lastRecordId) {
        Pageable pageable = PageRequest.of(0, listSize + 1, Sort.by("createdAt").descending());
        return recordRepository.findRecordsByKeyword(keyword, userId, lastRecordId, pageable);
    }
}
