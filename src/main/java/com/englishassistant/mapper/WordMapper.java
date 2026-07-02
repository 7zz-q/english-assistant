package com.englishassistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.englishassistant.entity.Word;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WordMapper extends BaseMapper<Word> {

    @Select("SELECT * FROM words WHERE user_id = #{userId} AND next_review <= CURDATE() ORDER BY next_review ASC LIMIT 30")
    List<Word> selectReviewWords(@Param("userId") Long userId);
}
