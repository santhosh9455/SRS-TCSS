package com.example.srs.Service;

import com.example.srs.DTO.NotificationDto;
import com.example.srs.DTO.NotificationResDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;


public interface NotificationService {
    NotificationResDto newNote(NotificationDto dto);

    List<NotificationResDto> getNote();

    NotificationResDto updateNote(Long id, NotificationDto dto);

    Map<String, String> deleteNote(Long id);

    Page<NotificationResDto> getNotes(int page, int size, String sortBy, String direction);

    Map<String, String> deleteNotes(List<Long> ids);
}
