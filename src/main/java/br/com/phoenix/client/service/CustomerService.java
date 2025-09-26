package br.com.phoenix.client.service;

import br.com.phoenix.client.model.Customer;
import br.com.phoenix.client.net.ApiHttpClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class CustomerService {

    private final ApiHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public CustomerService(ApiHttpClient http) {
        this.http = http;
    }

    public List<Customer> list() throws Exception {
        String json = http.get("/api/customers");
        return mapper.readValue(json, new TypeReference<List<Customer>>() {
        });
    }

    public Customer create(String name, String email) throws Exception {
        var node = mapper.createObjectNode();
        node.put("name", name);
        node.put("email", email);
        String json = http.postJson("/api/customers", mapper.writeValueAsString(node));
        return mapper.readValue(json, Customer.class);
    }

    public Customer update(Long id, String name, String email) throws Exception {
        var node = mapper.createObjectNode();
        node.put("id", id);
        node.put("name", name);
        node.put("email", email);
        String json = http.putJson("/api/customers/" + id, mapper.writeValueAsString(node));
        return mapper.readValue(json, Customer.class);
    }

    public void delete(Long id) throws Exception {
        http.delete("/api/customers/" + id);
    }
}
