package com.digitalnoticeboard.notice.dto;

import java.util.List;

public record StudentNoticeFiltersResponse(List<String> departments, List<String> academicYears) {
}
