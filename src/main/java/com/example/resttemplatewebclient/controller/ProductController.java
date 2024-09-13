package com.example.resttemplatewebclient.controller;

import com.example.resttemplatewebclient.api.apiResponse.Image;
import com.example.resttemplatewebclient.api.apiResponse.PagedResponse;
import com.example.resttemplatewebclient.api.apiResponse.Product;
import com.example.resttemplatewebclient.api.apiResponse.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class ProductController {

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final PagedListHolder pagedListHolder;

    private List<Product> productLists;


    @Autowired
    public ProductController(RestTemplate restTemplate, WebClient webClient, PagedListHolder pagedListHolder) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.pagedListHolder = pagedListHolder;
    }

    @GetMapping("/rest-template/product/{id}")
    public ResponseEntity<String> restTemplateGetSingleProduct(@PathVariable Long id){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
       return new ResponseEntity<>(restTemplate.exchange("https://dummyjson.com/products/" + id,
               HttpMethod.GET, httpEntity, String.class).getBody(), HttpStatus.OK);
    }


    @GetMapping("/webclient/product/{id}")
    public ResponseEntity<String> webClientGetSingleProduct(@PathVariable Long id){
        return new ResponseEntity<>(webClient.get()
                .uri("https://dummyjson.com/products/" + id)
                .retrieve().bodyToMono(String.class).block(), HttpStatus.OK);

    }

    @PostMapping("/rest-template/add/product/")
    public ResponseEntity<Product> templatePostAProduct(){
        //Create a new product object
        Product product = Product.builder()
                .title("Start Right Water Bottle")
                .description("To drink water with. ")
                .brand("plastic")
                .price(new BigDecimal(1499))
                .category("bottle")
                .rating(4.69)
                .stock(94)
                .thumbnail("bottle water")
                .images(List.of("www.images.com", "www.pics.com"))
                .build();

        // Set headers for the request, expecting JSON response
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        // Wrap the product and headers in an HttpEntity object
        HttpEntity<Product> entity = new HttpEntity<>(product, httpHeaders);

        // Send the POST request and return the response body
        return new ResponseEntity<>(restTemplate.exchange("https://dummyjson.com/products/add",
                HttpMethod.POST, entity, Product.class).getBody(), HttpStatus.CREATED);

    }


    @PostMapping("/webclient/add/product/")
    public ResponseEntity<Product> webClientAddProduct(){
        // Build a new Product object
        Product product = Product.builder()
                .title("Start Right Water Bottle")
                .description("To drink water with. ")
                .brand("plastic")
                .price(new BigDecimal(1499))
                .category("bottle")
                .rating(4.69)
                .stock(94)
                .thumbnail("bottle water")
                .images(List.of("www.images.com", "www.pics.com"))
                .build();

        // Send POST request using WebClient and return the response
        return new ResponseEntity<>(webClient.post()
                .uri("https://dummyjson.com/products/add")
                .bodyValue(product)
                .retrieve()
                .bodyToMono(Product.class)
                .block(),
                HttpStatus.CREATED);

    }


    @GetMapping("/webclient/products")
    public ResponseEntity<Mono<ProductResponse>> webClientGetAllProduct(){
        return new ResponseEntity<>(webClient.get()
                .uri("https://dummyjson.com/products/")
                .retrieve()
                .bodyToMono(ProductResponse.class),
                HttpStatus.OK);
    }

    @GetMapping("/rest-template/products")
    public ResponseEntity<List<Product>> restTemplateGetAllProduct(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        return new ResponseEntity<>(restTemplate.exchange("https://dummyjson.com/products/",
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<Product>>() {
                }).getBody(), HttpStatus.OK);
    }


    @GetMapping("/rest-template/paginated/products")
    public ResponseEntity<List<Product>> paginatedRestTemplateGetAllProduct() throws JsonProcessingException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        String productResponse = restTemplate.exchange("https://dummyjson.com/products/",
                HttpMethod.GET, entity, String.class).getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode productNode = objectMapper.readTree(productResponse).get("products");
        List<Product> productList = objectMapper.convertValue(productNode, new TypeReference<List<Product>>() {});
        productLists = productList;
        return new ResponseEntity<>(productList, HttpStatus.OK);
    }

    @GetMapping("/rest-template/paginated-products/{pageNo}")
    public ResponseEntity<PagedResponse<Product>> paginatedProductList(@PathVariable int pageNo){
        pagedListHolder.setPage(pageNo);
        pagedListHolder.setSource(productLists);
        PagedResponse<Product> productPagedResponse = new PagedResponse<>();
        productPagedResponse.setPagedList(pagedListHolder.getPageList());
        productPagedResponse.setPageNo(pagedListHolder.getPage());
        productPagedResponse.setPageSize(pagedListHolder.getPageSize());
        productPagedResponse.setTotalSize(pagedListHolder.getNrOfElements());

        return new ResponseEntity<>(productPagedResponse, HttpStatus.OK);

    }
}
