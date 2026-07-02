package com.englishassistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.englishassistant.entity.SeedWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface SeedWordMapper extends BaseMapper<SeedWord> {

    @Select("SELECT `level`, COUNT(*) as count FROM seed_words GROUP BY `level` ORDER BY count DESC")
    List<Map<String, Object>> selectLevelStats();
}
