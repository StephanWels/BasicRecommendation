package ste.wel.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RecommendationController {

    private static final String PATH = "recommend/main";

    List<SearchProduct> selectedItems = new LinkedList<>();

    @Autowired
    Recommender myRecommender;
    @Autowired
    ItemProvider itemProvider;

    @RequestMapping("/recommend")
    public ModelAndView greeting(@RequestParam final Optional<String> term) {
        final ModelAndView modelAndView = new ModelAndView(PATH);
        modelAndView.addObject("recommendations", myRecommender.recommendForItems(selectedItems));
        modelAndView.addObject("items", itemProvider.getItems(term));
        modelAndView.addObject("selectedItems", selectedItems);
        return modelAndView;
    }

    @RequestMapping("/addItem")
    public ModelAndView addItem(@RequestParam(value = "id", required = true) final Integer itemId) {
        if (!selectedItems.contains(itemProvider.getItem(itemId)))
            selectedItems.add(itemProvider.getItem(itemId));
        return greeting(Optional.empty());
    }

    @RequestMapping("/removeItem")
    public ModelAndView remove(@RequestParam(value = "id", required = true) final Integer itemId) {
        selectedItems.remove(itemProvider.getItem(itemId));
        return greeting(Optional.empty());
    }

    @RequestMapping("/explain")
    public ModelAndView explain(@RequestParam(value = "id", required = true) final Integer itemId) {

        final List<String> rule = myRecommender.explainForItem(selectedItems, itemId);
        final ModelAndView mav = new ModelAndView("recommend/explain");
        mav.addObject("rules", rule);
        return mav;
    }
}
