package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.services.impl.PageServiceImpl;

import java.sql.SQLIntegrityConstraintViolationException;

@Controller
public class DefaultController {

    private final SitesList sites;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    public DefaultController(SitesList sites) {
        this.sites = sites;
    }


    /**
     * Метод формирует страницу из HTML-файла index.html,
     * который находится в папке resources/templates.
     * Это делает библиотека Thymeleaf.
     */
    @RequestMapping("/index")
    public String index() throws SQLIntegrityConstraintViolationException, InterruptedException {
        PageServiceImpl pageService = new PageServiceImpl(sites,siteRepository,pageRepository);
        pageService.init();

        return "index";
    }

}
