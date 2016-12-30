package com.kepler.async;

/**
 * Created by 张皆浩 on 2016/12/26.
 * DIDI CORPORATION
 */
class StackLoop {

    ConcurrentStack<AbstractStage<?, ?>> stack = new ConcurrentStack<>();

    void pushStage(AbstractStage stage) {
        this.stack.push(stage);
    }

    void postFire() {
        AbstractStage<?, ?> stage = null;
        while ((stage = this.stack.pop()) != null) {
            stage.exec();
        }
    }

}
