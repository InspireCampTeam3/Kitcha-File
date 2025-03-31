package com.kitcha.file.repository;

import com.kitcha.file.entity.File;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends CrudRepository<File, Long> {
    File findByBoardId(Long boardId);
}
