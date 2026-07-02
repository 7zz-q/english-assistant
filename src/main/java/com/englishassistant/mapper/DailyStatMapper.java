package com.englishassistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.englishassistant.entity.DailyStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyStatMapper extends BaseMapper<DailyStat> {

    @Select("SELECT `date` FROM daily_stats WHERE user_id = #{userId} ORDER BY `date` DESC LIMIT 100")
    List<LocalDate> selectDatesByUserId(@Param("userId") Long userId);
}
