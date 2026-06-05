package com.ddogalmap.domain.users.repository;

import com.ddogalmap.domain.users.dto.TasteExpertSearchCondition;
import com.ddogalmap.domain.users.dto.response.TasteExpertPageResponse;

public interface TasteExpertQueryRepository {

    TasteExpertPageResponse search(TasteExpertSearchCondition condition);
}
