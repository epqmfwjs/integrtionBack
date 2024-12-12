package hello.integration.service;

import hello.integration.domain.Announcement;
import hello.integration.domain.UpdateRepository;
import hello.integration.repository.UpdateRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService {
    private final UpdateRepository updateRepository;

    public List<UpdateRequestDTO> getAllUpdates() {
        List<Announcement> updates = updateRepository.findAll();
        return updates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UpdateRequestDTO> getAllUpdatesSortedByDateDesc() {
        List<Announcement> updates = updateRepository.findAllByOrderByDateDesc();
        return updates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UpdateRequestDTO getUpdateById(Long id) {
        return updateRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Update not found with id: " + id));
    }

    @Transactional
    public UpdateRequestDTO createUpdate(UpdateRequestDTO updateDTO) {
        Announcement update = new Announcement(
                updateDTO.getDate(),
                updateDTO.getTitle(),
                updateDTO.getContent()
        );
        Announcement savedUpdate = updateRepository.save(update);
        return convertToDTO(savedUpdate);
    }

    private UpdateRequestDTO convertToDTO(Announcement update) {
        UpdateRequestDTO dto = new UpdateRequestDTO();
        dto.setId(update.getId());
        dto.setDate(update.getDate());
        dto.setTitle(update.getTitle());
        dto.setContent(update.getContent());
        return dto;
    }

    @Transactional
    public UpdateRequestDTO updateUpdate(Long id, UpdateRequestDTO updateDTO) {
        Announcement announcement = updateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Update not found"));

        announcement.setTitle(updateDTO.getTitle());
        announcement.setContent(updateDTO.getContent());

        Announcement savedUpdate = updateRepository.save(announcement);
        return convertToDTO(savedUpdate);
    }

    @Transactional
    public void deleteUpdate(Long id) {
        updateRepository.deleteById(id);
    }

}
