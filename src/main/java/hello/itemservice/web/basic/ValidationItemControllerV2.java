package hello.itemservice.web.basic;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) { // @Validated가 붙은 객체의 검증기가 작동되게 한다.
        dataBinder.addValidators(itemValidator); // WebDataBinder에 Item 검증기 추가
        // @ModelAttribute Item item에 @Validated가 붙어 자동으로 itemValidator.validate()가 실행됨.
        // Item 객체 말고도 다른 객체의 검증기도 추가할 수 있다. ex) dataBinder.addValidators(UserValidator);
        // 그렇다면 많은 검증기가 등록이 돼있는데 어떤 객체의 검증기인지 어떻게 알고 해당되는 검증기를 작동하느냐?
        // 검증기 클래스의 implements Validator에서 supports라는 메소드가 해당되는 클래스를 찾고 맞으면 true 후 validate 메소드가 실행됨.
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    /**
     * 테스트용 데이터 추가
     */

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addView(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
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
        return "validation/v2/item";
    }

    //@PostMapping("/add")
    public String saveV2(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        itemRepository.save(item);
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        return "validation/v2/item";
    }

    //@PostMapping("/add")
    public String saveV3(@ModelAttribute Item item) { // 사실 파라미터에 item을 생략해도 알아서 model에 Item(클래스명)의 첫글자만 소문자로 바꿔서 저장이 된다.
        //model.addAttribute("item", item); ModelAttribute를 쓰면 알아서 model에 담김.
        itemRepository.save(item);
        return "redirect:/validation/v2/items/" + item.getId(); //
    }

    //@PostMapping("/add")
    public String addItemV1(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
            // objectName은 ModelAttribute를 쓴 객체의 변수명, field는 객체의 오류가 발생한 속성이름, defaultMessage는 오류 메시지
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1000원에서 1000000까지 허용합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9999개까지 허용합니다."));
        }
        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 x 수량의 합은 10000원 이상이어야 합니다.")); // 글로벌오류
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v2/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    //@PostMapping("/add")
    public String addItemV2(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            //bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
            // objectName은 ModelAttribute를 쓴 객체의 변수명, field는 객체의 오류가 발생한 속성이름, defaultMessage는 오류 메시지
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
            //검증에서 실패 시 값을 그대로 다시 보여주기 위함.
            //FieldError의 생성자는 2개이다. 세 번째 파라미터는 검증이 실패한 필드의 값, 4번째는 ModelAttribute 바인딩이 실패했는지 ex)타입 오류,
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            //bindingResult.addError(new FieldError("item", "price", "가격은 1000원에서 1000000까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1000원에서 1000000까지 허용합니다."));

        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            //bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9999개까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null, "수량은 최대 9999개까지 허용합니다."));
        }
        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 x 수량의 합은 10000원 이상이어야 합니다.")); // 글로벌오류
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v2/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    //@PostMapping("/add") // 오류 코드와 메시지 처리1
    public String addItemV3(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            //bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
            // objectName은 ModelAttribute를 쓴 객체의 변수명, field는 객체의 오류가 발생한 속성이름, defaultMessage는 오류 메시지
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
            //검증에서 실패 시 값을 그대로 다시 보여주기 위함.
            //FieldError의 생성자는 2개이다. 세 번째 파라미터는 검증이 실패한 필드의 값, 4번째는 ModelAttribute 바인딩이 실패했는지 ex)타입 오류,
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            //bindingResult.addError(new FieldError("item", "price", "가격은 1000원에서 1000000까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));

        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            //bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9999개까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, "수량은 최대 9999개까지 허용합니다."));
        }
        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null)); // 글로벌오류
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v2/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    //@PostMapping("/add") // 오류 코드와 메시지 처리1
    public String addItemV4(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required", null); // errors.properties의 required.item.itemName에 있는 값 가져옴.
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range",new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }
        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v2/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    //@PostMapping("/add") // 오류 코드와 메시지 처리1
    public String addItemV5(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        itemValidator.validate(item, bindingResult); // 검증 클래스를 따로 만들어 검증 로직을 컨트롤러 코드에서 처리하지 않게함.

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v2/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    @PostMapping("/add") // 최종 검증 메소드
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) { // BindingResult는 ModelAttribute 바로 뒤에 와야 함.

        //itemValidator.validate(item, bindingResult); // 검증 클래스를 따로 만들어 검증 로직을 컨트롤러 코드에서 처리하지 않게함.
        // 파라미터 Item item에서 @Validated를 넣으면 itemValidator.validate 메소드를 수행해줌.

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { // 에러에 값이 담겨있으면 검증에 걸린 것
            log.info("errors = {}", bindingResult);
            //model.addAttribute("errors", errors); view 처리에 bindingResult 값이 같이 넘어감. 그래서 html에서 쓸 수 있음.
            return "validation/v2/addForm"; // 상품 추가 입력 폼
        }

        //상품 입력 성공
        Item saved = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", saved.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}"; // RedirectAttribute를 쓰면 itemId가 자동으로 {itemId}에 치환이 되고 남은 status 값은 파라미터로 넘어가게 된다
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable("itemId") Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String editItem(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}"; //PathVariable의 itemId 값이 자동으로 {itemId}에 바인딩됨.
    }

    //@PostMapping
    //@PostConstruct
    public void init() {
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));
    }
}
