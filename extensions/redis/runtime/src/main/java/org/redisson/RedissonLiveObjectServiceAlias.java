package org.redisson;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.redisson.command.CommandAsyncExecutor;

@TargetClass(RedissonLiveObjectService.class)
public final class RedissonLiveObjectServiceAlias {

    @Substitute
    private <T> Class<? extends T> createProxy(Class<T> entityClass, CommandAsyncExecutor commandExecutor) {
        throw new UnsupportedOperationException(
                "RedissonLiveObjectService.createProxy(Class<T>, CommandAsyncExecutor) is not supported in native mode");
    }
}
