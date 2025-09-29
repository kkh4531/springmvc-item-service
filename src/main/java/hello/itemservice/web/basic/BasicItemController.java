package hello.itemservice.web.basic;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/basic/items")
@RequiredArgsConstructor
public class BasicItemController {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "basic/items";
    }

    /**
     * 테스트용 데이터 추가
     */

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "basic/item";
    }

    @GetMapping("/add")
    public String addView() {
        return "basic/addForm";
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
        return "basic/item";
    }

    @PostMapping("/add")
    public String saveV2(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        itemRepository.save(item);
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        return "basic/item";
    }

    @PostConstruct
    public void init() {
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemA", 20000, 20));
    }
}
