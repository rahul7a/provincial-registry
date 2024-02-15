package com.lblw.vphx.phms.common.repositories;

import com.lblw.vphx.phms.common.logs.PageListLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageListLogRepository extends ReactiveMongoRepository<PageListLog, ObjectId> {}
