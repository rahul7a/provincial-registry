package com.lblw.vphx.phms.common.repositories;

import com.lblw.vphx.phms.domain.common.logs.Message;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<Message, ObjectId> {
  Mono<Message> findByRequestIdAndMessageProcessAndMessageType(
      String requestId, String messageProcess, String messageType);
}
