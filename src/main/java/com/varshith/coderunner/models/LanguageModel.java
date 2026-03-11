package com.varshith.coderunner.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LanguageModel {

    @Id
    @Column(name="languageId")
    private int languageId;

    private String languageName;

    private double languageMultiplier;
}