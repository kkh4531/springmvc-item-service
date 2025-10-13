package hello.itemservice.web.basic;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    /**
     * 테스트용 데이터 추가
     */

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addView(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
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
        return "validation/v1/item";
    }

    //@PostMapping("/add")
    public String saveV2(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        itemRepository.save(item);
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        return "validation/v1/item";
    }

    //@PostMapping("/add")
    public String saveV3(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        itemRepository.save(item);
        return "redirect:/validation/v1/items/" + item.getId(); //
    }

    @PostMapping("/add")
    public String saveV4(Item item, RedirectAttributes redirectAttributes, Model model) { // @ModelAttribute생략 가능

        //검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();
        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "상품 이름은 필수입니다.");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.put("price", "가격은 1000원에서 1000000까지 허용합니다.");
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            errors.put("quantity", "수량은 최대 9999개까지 허용합니다.");
        }
        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                errors.put("globalError", "가격 x 수량의 합은 10000원 이상이어야 합니다." + resultPrice);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (!errors.isEmpty()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", errors);
            model.addAttribute("errors", errors);
            return "validation/v1/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable("itemId") Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String editItem(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}"; //PathVariable의 itemId 값이 자동으로 {itemId}에 바인딩됨.
    }

    @PostMapping
    @PostConstruct
    public void init() {
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));
    }
}
