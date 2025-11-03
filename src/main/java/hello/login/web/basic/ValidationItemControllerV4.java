package hello.login.web.basic;

import hello.login.domain.item.Item;
import hello.login.domain.item.ItemRepository;
import hello.login.web.basic.form.ItemSaveForm;
import hello.login.web.basic.form.ItemUpdateForm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    /**
     * 테스트용 데이터 추가
     */

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addView(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    /*
        * **@ModelAttribute - 요청 파라미터 처리**
    `@ModelAttribute` 는 `Item` 객체를 생성하고, 요청 파라미터의 값을 프로퍼티 접근법(setXxx)으로 입력해준다.
    **@ModelAttribute - Model 추가**
    `@ModelAttribute` 는 중요한 한가지 기능이 더 있는데, 바로 모델(Model)에 `@ModelAttribute`
    로 지정한 객체
    를 자동으로 넣어준다. 지금 코드를 보면 `model.addAttribute("item", item)` 가 주석처리 되어 있어도 잘 동
    작하는 것을 확인할 수 있다.
    모델에 데이터를 담을 때는 이름이 필요하다. 이름은 `@ModelAttribute` 에 지정한 `name(value)` 속성을 사용한
    다. 만약 다음과 같이 `@ModelAttribute` 의 이름을 다르게 지정하면 다른 이름으로 모델에 포함된다.
    `@ModelAttribute("hello") Item item` 이름을 `hello`
    로 지정
    `model.addAttribute("hello", item);` 모델에 `hello` 이름으로 저장
    */
    //@PostMapping("/add")
    public String saveV1(@ModelAttribute("item") Item item) { // ModelAttribute 파라미터에 이름을 쓰면 이것은 자동으로 model.addAttribute("item", item)으로 담긴다.
        itemRepository.save(item);
        //model.addAttribute("item", item); ModelAttribute 파라미터에 쓰면 자동으로 담김
        return "validation/v4/item";
    }

    //@PostMapping("/add")
    public String saveV2(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        itemRepository.save(item);
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        return "validation/v4/item";
    }

    //@PostMapping("/add")
    public String saveV3(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        itemRepository.save(item);
        return "redirect:/validation/v4/items/" + item.getId(); //
    }

    @PostMapping("/add") // 최종 검증 메소드
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        // Bean Validation을 이용할 때 Item 클래스에서 애노테이션을 이용해 검증을 하는데 이때 검증은 무조건 바인딩이 됐다는 전제 하에 검증한다.
        // 그 말은 즉, 애초에 타입 에러로 바인딩이 실패될 때는 Bean Validation이 검증을 하지 못한다.
        // errors.properties에 typeMismatch를 해놓았다면 해당 에러 메시지가 뜰 것이지만 해놓지 않았다면 스프링 내부 디폴트 메시지가 나가게 됨.

        //특정 필드가 아닌 복합 룰 검증
        //객체의 단순 필드 검증이 아닌 복합적인 값을 이용한 검증은에서 ScriptAssert는 제약이 많아서 그냥 코드로 해결하는 게 낫다.
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v4/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item item = new Item(form.getItemName(), form.getPrice(), form.getQuantity());
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable("itemId") Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String editItem(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm itemUpdateForm, BindingResult bindingResult) {

        if (itemUpdateForm.getPrice() != null && itemUpdateForm.getQuantity() != null) {
            int resultPrice = itemUpdateForm.getPrice() * itemUpdateForm.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "validation/v4/editForm";
        }
        Item item = new Item(itemUpdateForm.getItemName(), itemUpdateForm.getPrice(), itemUpdateForm.getQuantity());
        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}"; //PathVariable의 itemId 값이 자동으로 {itemId}에 바인딩됨.
    }

    //@PostMapping
    @PostConstruct
    public void init() {
        log.info("validation/v4");
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemA", 20000, 20));
    }
}
