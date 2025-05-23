import net.fpsboost.event.Event;
import net.fpsboost.event.EventPriority;
import net.fpsboost.event.EventTarget;
import net.fpsboost.manager.impl.EventManager;

// test代码是ai https://chatgpt.com/share/6830ceca-659c-8010-a803-473346dcfd9f
public class EventManagerTest {

    // 模拟一个简单的事件类
    public static class TestEvent extends Event {}

    // 测试监听器
    public static class TestListener {
        public boolean called = false;

        @EventTarget(priority = EventPriority.NORMAL)
        public void onEvent(TestEvent e) {
            called = true;
            System.out.println("TestListener received event.");
        }
    }

    public static class SecondListener {
        public boolean called = false;

        @EventTarget(priority = EventPriority.VERY_HIGH)
        public void handle(TestEvent e) {
            called = true;
            System.out.println("SecondListener received event with HIGH priority.");
        }
    }

    public static void main(String[] args) {
        EventManager eventManager = new EventManager();

        TestListener listener = new TestListener();
        SecondListener highPriorityListener = new SecondListener();

        System.out.println("== 测试注册与事件调用 ==");
        eventManager.register(listener);
        eventManager.call(new TestEvent());
        assert listener.called : "TestListener 应该接收到事件";

        System.out.println("== 测试注销 ==");
        listener.called = false;
        eventManager.unregister(listener);
        eventManager.call(new TestEvent());
        assert !listener.called : "TestListener 不应该再接收到事件";

        System.out.println("== 测试优先级调用顺序 ==");
        eventManager.register(listener);
        eventManager.register(highPriorityListener);
        listener.called = false;
        highPriorityListener.called = false;
        eventManager.call(new TestEvent());
        assert highPriorityListener.called : "高优先级监听器应该被调用";
        assert listener.called : "普通优先级监听器也应该被调用";

        System.out.println("== 所有测试通过 ✅ ==");
    }
}
