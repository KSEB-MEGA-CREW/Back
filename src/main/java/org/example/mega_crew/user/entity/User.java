package org.example.mega_crew.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
public class User {

  @Id
  private Long user_id;

  @Column
  private String email;

  @Column
  private String password;

  @Column
  private String hearing_staus;



}
