package org.example.mega_crew.domain.question.entity;

public enum TicketStatus {
   PENDING("대기 중"),
   IN_PROGRESS("처리 중"),
   ANSWERED("답변 완료"),
   CLOSED("종료됨");

   private final String description;

   TicketStatus(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }
}
