package com.example.srs.ServiceImplementation;


import com.example.srs.DTO.NotificationDto;
import com.example.srs.DTO.NotificationResDto;
import com.example.srs.Model.NotificationEntity;
import com.example.srs.Model.StudentEntity;
import com.example.srs.Model.UserProfileEntity;
import com.example.srs.Repository.NotificationRepo;
import com.example.srs.Repository.StudentRepo;
import com.example.srs.Repository.UserProfileRepo;
import com.example.srs.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImp implements NotificationService {

   @Autowired
   private NotificationRepo noteRepo;

   @Autowired
   private StudentRepo studentRepo;

   @Autowired
   private UserProfileRepo userProfileRepo;


    @Override
    public NotificationResDto newNote(NotificationDto dto) {
        NotificationEntity note = new NotificationEntity();

        note.setMessage(dto.getMessage());

        if (dto.getStudentId() != null){
            StudentEntity student = studentRepo.findById(dto.getStudentId())
                    .orElseThrow(()-> new RuntimeException("Student not found"));
            note.setStudent(student);
        }

        if (dto.getProfileId() != null){
            UserProfileEntity staff = userProfileRepo.findById(dto.getProfileId())
                    .orElseThrow(()-> new RuntimeException("staff not found"));
            note.setProfile(staff);
        }

        NotificationEntity saved = noteRepo.save(note);

        return mapToDto(saved);
    }

    @Override
    public List<NotificationResDto> getNote() {
        List<NotificationEntity> note = noteRepo.findAllByOrderBySentAtDesc();
        return note.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResDto updateNote(Long id, NotificationDto dto) {
        NotificationEntity note = noteRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Notification not found"));
        if (dto.getMessage() != null){
            note.setMessage(dto.getMessage());
        }
        if (dto.getProfileId() != null){
            UserProfileEntity userProfile = userProfileRepo.findById(dto.getProfileId())
                    .orElseThrow(()->new RuntimeException("Staff not found"));
            note.setProfile(userProfile);
        }
        if (dto.getStudentId() != null){
            StudentEntity student = studentRepo.findById(dto.getStudentId())
                    .orElseThrow(()->new RuntimeException("Student not found"));
            note.setStudent(student);
        }
        NotificationEntity saved = noteRepo.save(note);

        return mapToDto(saved);
    }

    @Override
    public Map<String, String> deleteNote(Long id) {
        NotificationEntity note = noteRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Note not found"));
        note.setStudent(null);
        note.setProfile(null);
        noteRepo.delete(note);
        Map<String,String> res = new HashMap<>();
        res.put("message","Note deleted successfully");
        return res;

    }

    @Override
    public Page<NotificationResDto> getNotes(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<NotificationEntity> notifications;


            notifications = noteRepo.findAll(pageable);


        return notifications.map(this::mapToDto);
    }

    @Override
    public Map<String, String> deleteNotes(List<Long> ids) {
        // Fetch all notes by given ids
        List<NotificationEntity> notes = noteRepo.findAllById(ids);

        // If size mismatch, means some IDs not found
        if (notes.size() != ids.size()) {
            throw new RuntimeException("One or more notification IDs not found");
        }

        // Detach relations before delete
        for (NotificationEntity note : notes) {
            note.setStudent(null);
            note.setProfile(null);
        }

        // Bulk delete
        noteRepo.deleteAll(notes);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Deleted " + notes.size() + " notifications successfully");
        return res;
    }


    private NotificationResDto mapToDto(NotificationEntity saved) {

        NotificationResDto dto = new NotificationResDto();

        dto.setNotificationId(saved.getNotificationId());
        dto.setMessage(saved.getMessage());
        if (saved.getProfile() != null){
            dto.setProfileId(saved.getProfile().getId());
            dto.setProfileName(saved.getProfile().getName());
        }
        if (saved.getStudent() != null){
            dto.setStudentId(saved.getStudent().getId());
            dto.setStudentName(saved.getStudent().getFirstName());
        }
        dto.setMessage(saved.getMessage());
        dto.setSentAt(saved.getSentAt());

        return dto;
    }
}
