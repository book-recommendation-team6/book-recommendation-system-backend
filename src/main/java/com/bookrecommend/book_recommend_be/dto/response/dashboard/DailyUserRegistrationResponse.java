package com.bookrecommend.book_recommend_be.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyUserRegistrationResponse {
    private LocalDate date;
    private Long count;
}
