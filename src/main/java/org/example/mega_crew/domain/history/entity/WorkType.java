package org.example.mega_crew.domain.history.entity;

public enum WorkType {

   IMAGETOTEXT("IMAGE_TO_TEXT", "이미지 → 텍스트 (수어 번역)"),
   TEXTTO3D("TEXT_TO_3D", "텍스트 → 3D (수어 생성)");

   private final String code;
   private final String description;

   WorkType(String code, String description) {
      this.code = code;
      this.description = description;
   }

   public String getCode() { return code; }
   public String getDescription() { return description; }
}
