package com.alumni.back.tag;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface TagMapper {
    @Select("SELECT * FROM tag ORDER BY id DESC LIMIT #{offset}, #{pageSize}")
    List<Map<String, Object>> selectTagList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM tag")
    int countTagList();

    @Select("SELECT COUNT(*) FROM alumnus WHERE FIND_IN_SET(#{tagId}, tag_ids)")
    int countAlumniByTagId(@Param("tagId") int tagId);

    @Insert("INSERT INTO tag (name, color, create_time, update_time) VALUES (#{name}, #{color}, NOW(), NOW())")
    int insertTag(@Param("name") String name, @Param("color") String color);

    @Delete("DELETE FROM tag WHERE id = #{id}")
    int deleteTagById(@Param("id") int id);

    @Update("UPDATE tag SET name = #{name}, color = #{color}, update_time = NOW() WHERE id = #{id}")
    int updateTag(@Param("id") Integer id, @Param("name") String name, @Param("color") String color);

    @Select("SELECT id FROM tag WHERE name = #{name} LIMIT 1")
    Integer selectTagIdByName(@Param("name") String name);
    
    @Select("SELECT name FROM tag WHERE id = #{id}")
    String selectTagNameById(@Param("id") Integer id);

    int deleteById(Integer id);
} 