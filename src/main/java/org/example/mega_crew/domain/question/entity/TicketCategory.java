package org.example.mega_crew.domain.question.entity;

public enum TicketCategory {

   TECHNICAL("기술적 문제"),
   ACCOUNT("계정 관련"),
   BILLING("결제 관련"),
   FEATURE("기능 요청"),
   BUG("버그 신고"),
   OTHER("기타");

   private final String description;

   TicketCategory(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }
}
