package com.example.srs.Controller;


import com.example.srs.DTO.NotificationDto;
import com.example.srs.DTO.NotificationResDto;
import com.example.srs.ServiceImplementation.NotificationServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    @Autowired
    private NotificationServiceImp notify;

    @PostMapping("/newNote")
    private ResponseEntity<NotificationResDto> newNote(@RequestBody NotificationDto dto){

        return ResponseEntity.ok(notify.newNote(dto));
    }

    @GetMapping("/getNote")
    private ResponseEntity<List<NotificationResDto>> getNote(){
        return ResponseEntity.ok(notify.getNote());
    }

    @GetMapping("/getNotes")
    private ResponseEntity<Page<NotificationResDto>> getNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction ) {

        Page<NotificationResDto> notes = notify.getNotes(page, size, sortBy, direction);
        return ResponseEntity.ok(notes);
    }


    @PatchMapping("/update/note/{id}")
    private ResponseEntity<NotificationResDto> updateNote(@PathVariable Long id,@RequestBody NotificationDto dto){

        return ResponseEntity.ok(notify.updateNote(id,dto));
    }

    @DeleteMapping("/delete/note/{id}")
    private ResponseEntity<Map<String,String>> deleteNote(@PathVariable Long id){
        return ResponseEntity.ok(notify.deleteNote(id));
    }

    @DeleteMapping("/delete/bulk")
    public ResponseEntity<Map<String, String>> deleteNotes(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(notify.deleteNotes(ids));
    }
}
