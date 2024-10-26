package corecord.dev.domain.record.service;

import corecord.dev.domain.folder.entity.Folder;
import corecord.dev.domain.folder.exception.enums.FolderErrorStatus;
import corecord.dev.domain.folder.exception.model.FolderException;
import corecord.dev.domain.folder.repository.FolderRepository;
import corecord.dev.domain.record.converter.RecordConverter;
import corecord.dev.domain.record.dto.request.RecordRequest;
import corecord.dev.domain.record.dto.response.RecordResponse;
import corecord.dev.domain.record.entity.Record;
import corecord.dev.domain.record.exception.enums.RecordErrorStatus;
import corecord.dev.domain.record.exception.model.RecordException;
import corecord.dev.domain.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final FolderRepository folderRepository;

    @Transactional
    public RecordResponse.MemoRecordDto createMemoRecord(RecordRequest.MemoRecordDto recordDto) {
        String title = recordDto.getTitle();
        String content = recordDto.getContent();
        Folder folder = findFolderById(recordDto.getFolderId());

        validateTitleLength(title);

        // TODO: USER MAPPING
        Record record = RecordConverter.toMemoRecordEntity(title, content, null, folder);
        recordRepository.save(record);

        return RecordConverter.toMemoRecordDto(record);
    }

    private void validateTitleLength(String title) {
        if (title.length() > 15) {
            throw new RecordException(RecordErrorStatus.OVERFLOW_MEMO_RECORD_TITLE);
        }
    }

    private Folder findFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderException(FolderErrorStatus.FOLDER_NOT_FOUND));
    }
}
