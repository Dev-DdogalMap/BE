package com.ddogalmap.domain.reviews.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    // 이미지가 저장될 경로
    private final String uploadPath = "E:/백엔드8회차/4차협업프젝/ddogalmap/TestDir/";

    public String saveFile(MultipartFile file) {
        if (file.isEmpty()) return null;

        // 저장할 폴더가 없으면 생성
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일명 중복 방지를 위해 UUID 사용
        String originalFilename = file.getOriginalFilename();
        String storeFilename = UUID.randomUUID() + "_" + originalFilename;

        // 파일 저장
        try {
            file.transferTo(new File(uploadPath + storeFilename));
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        // 저장된 파일명(또는 경로) 반환
        return storeFilename;
    }
}
