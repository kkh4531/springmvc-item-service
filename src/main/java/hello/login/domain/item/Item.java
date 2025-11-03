package hello.login.domain.item;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@ScriptAssert(lang = "javascript", script = "_this.price * this.quantity >= 10000")
public class Item {

    //@NotNull(groups = UpdateCheck.class)
    private Long id;

    // Bean Validation을 이용할 때 Item 클래스에서 애노테이션을 이용해 검증을 하는데 이때 검증은 무조건 바인딩이 됐다는 전제 하에 검증한다.
    // 그 말은 즉, 애초에 타입 에러로 바인딩이 실패될 때는 Bean Validation이 검증을 하지 못한다.
    // errors.properties에 typeMismatch를 해놓았다면 해당 에러 메시지가 뜰 것이지만 해놓지 않았다면 스프링 내부 디폴트 메시지가 나가게 됨.
    //@NotBlank(groups = {SaveCheck.class, UpdateCheck.class}, message = "공백일 수 없습니다.")
    private String itemName;

    //@NotNull(groups = {SaveCheck.class, UpdateCheck.class}, message = "공백일 수 없습니다.")
    //@Range(min = 1000, max = 1000000, message = "1000 ~ 1000000 까지만 허용합니다.")
    private Integer price;

    //@NotNull(groups = {SaveCheck.class, UpdateCheck.class}, message = "공백일 수 없습니다.")
    //@Max(value = 9999, groups = SaveCheck.class, message = "9999 까지만 허용합니다.")
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
