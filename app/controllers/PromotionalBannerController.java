package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.PromotionsBanner;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.fileUtils;

import java.io.File;
import java.util.List;


/**
 * Created by Wahid on 10/3/17.
 */
public class PromotionalBannerController extends BaseController {

    public Result addPromotionalBanner() {
        return ok(views.html.uploadPromotionalBanner.render());
    }

    public Result uploadPromotionalBanner() {
        ObjectNode objectNode = Json.newObject();
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        try {
            DynamicForm requestData = formFactory.form().bindFromRequest();
            String promotionUrl = requestData.get("url");
            Http.MultipartFormData body = request().body().asMultipartFormData();
            PromotionsBanner bannerFile = new PromotionsBanner();
            if (body == null) {
                return badRequest("Invalid request, required is POST with enctype=multipart/form-data.");
            }
            String hdpiImage = fileUtils.fileUpload(body.getFile("hdpiImage"), promotionUrl);
            String ldpiImage = fileUtils.fileUpload(body.getFile("ldpiImage"), promotionUrl);
            String mdpiImage = fileUtils.fileUpload(body.getFile("mdpiImage"), promotionUrl);
            String xhdpiImage = fileUtils.fileUpload(body.getFile("xhdpiImage"), promotionUrl);
            String xxhdpiImage = fileUtils.fileUpload(body.getFile("xxhdpiImage"), promotionUrl);
            bannerFile.setPromotionsURL(promotionUrl);System.out.println("Url ---->" + promotionUrl);
            bannerFile.setHdpiPromotionalBanner(hdpiImage);System.out.println("File ---->" + hdpiImage);
            bannerFile.setLdpiPromotionalBanner(ldpiImage);System.out.println("File ---->" + ldpiImage);
            bannerFile.setMdpiPromotionalBanner(mdpiImage);System.out.println("File ---->" + mdpiImage);
            bannerFile.setXhdpiPromotionalBanner(xhdpiImage);System.out.println("File ---->" + xhdpiImage);
            bannerFile.setXxhdpiPromotionalBanner(xxhdpiImage);System.out.println("File ---->" + xxhdpiImage);
            bannerFile.save();
            objectNode.put(SUCCESS, SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put(FAILURE, FAILURE);
        }
        Logger.info("Status : " + objectNode);
        return redirect("/allPromotion");
    }

    public Result promotionBannerList() {
        return ok(views.html.promotionBannerList.render());
    }

    public Result getAllPromotionBanner() {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        List<PromotionsBanner> promotionsBannerList = PromotionsBanner.find.orderBy("id").findList();
        objectNode.put("size", PromotionsBanner.find.all().size());
        setResult(objectNode, promotionsBannerList);
        return ok(Json.toJson(objectNode));
    }

    public Result deletePromotionalBanner(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        try {
            PromotionsBanner bannerFile = PromotionsBanner.find.byId(id);
            fileUtils.delete(new File("public/uploads/", bannerFile.getHdpiPromotionalBanner()));
            fileUtils.delete(new File("public/uploads/", bannerFile.getLdpiPromotionalBanner()));
            fileUtils.delete(new File("public/uploads/", bannerFile.getMdpiPromotionalBanner()));
            fileUtils.delete(new File("public/uploads/", bannerFile.getXhdpiPromotionalBanner()));
            fileUtils.delete(new File("public/uploads/", bannerFile.getXxhdpiPromotionalBanner()));
            bannerFile.delete();
            objectNode.put("success", SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put("failure", FAILURE);
        }
        Logger.info("Status : " + objectNode);
        return redirect("/allPromotion");
    }

    public Result applyPromotionalBanner(Long id) {
        if (!isValidateSession()) {
            return redirect(routes.LoginController.login());
        }
        ObjectNode objectNode = Json.newObject();
        try {
            PromotionsBanner bannerFile = PromotionsBanner.find.byId(id);
            if (bannerFile.getShowThisBanner().equals(false)) {
                List<PromotionsBanner> banners = PromotionsBanner.find.all();
                for (PromotionsBanner banner : banners) {
                    banner.setShowThisBanner(false);
                    banner.update();
                }
                bannerFile.setShowThisBanner(true);
                bannerFile.update();
            } else {
                List<PromotionsBanner> banners = PromotionsBanner.find.all();
                for (PromotionsBanner banner : banners) {
                    banner.setShowThisBanner(false);
                    banner.update();
                }
            }
            objectNode.put("success", SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            objectNode.put("failure", FAILURE);
        }
        Logger.info("Status : " + objectNode);
        return redirect("/allPromotion");

    }

    public Result sendPromotionalBannerWithUrl() {
        ObjectNode objectNode = Json.newObject();
        String result = FAILURE;
        String imageResolution = getString("resolution");
        List<PromotionsBanner> promotionsBanners = PromotionsBanner.find.all();
        for (PromotionsBanner promotionsBanner: promotionsBanners) {
            if (promotionsBanner.getShowThisBanner().equals(true)){
                switch (imageResolution){
                    case "hdpi" : objectNode.put("hdpi",promotionsBanner.getHdpiPromotionalBanner());
                        break;
                    case "ldpi" : objectNode.put("ldpi",promotionsBanner.getLdpiPromotionalBanner());
                        break;
                    case "mdpi" : objectNode.put("mdpi",promotionsBanner.getMdpiPromotionalBanner());
                        break;
                    case "xhdpi" : objectNode.put("xhdpi",promotionsBanner.getXhdpiPromotionalBanner());
                        break;
                    case "xxhdpi" : objectNode.put("xxhdpi",promotionsBanner.getXxhdpiPromotionalBanner());
                        break;
                    default: System.out.println("NO images found for promotional banner!");
                }
                objectNode.put("promotionsURL",promotionsBanner.getPromotionsURL());
                result = SUCCESS;
            }
        }
        setResult(objectNode, result);
        return ok(Json.toJson(objectNode));
    }
}
