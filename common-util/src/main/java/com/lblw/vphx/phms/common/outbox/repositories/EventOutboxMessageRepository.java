package com.lblw.vphx.phms.common.outbox.repositories;

import com.lblw.vphx.phms.common.outbox.entity.EventOutboxMessage;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Data repository for {@link EventOutboxMessage} */
@Repository
public interface EventOutboxMessageRepository
    extends MongoRepository<EventOutboxMessage, ObjectId> {}
