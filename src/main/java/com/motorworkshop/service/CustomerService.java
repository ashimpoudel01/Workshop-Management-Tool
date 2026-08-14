package com.motorworkshop.service;

import com.motorworkshop.dao.CustomerDAO;
import com.motorworkshop.model.Customer;
import com.motorworkshop.util.DateUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic service for managing Customers and vehicle history.
 */
public class CustomerService {
    private final CustomerDAO customerDAO = new CustomerDAO();

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerById(int id) throws SQLException {
        return customerDAO.getCustomerById(id);
    }

    public List<Customer> searchCustomers(String query) throws SQLException {
        return customerDAO.searchCustomers(query);
    }

    public boolean addCustomer(Customer c) throws SQLException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required!");
        }
        if (c.getPhone() == null || c.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required!");
        }
        c.setCreatedAt(DateUtil.today());
        return customerDAO.insertCustomer(c);
    }

    public boolean updateCustomer(Customer c) throws SQLException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required!");
        }
        return customerDAO.updateCustomer(c);
    }

    public boolean deleteCustomer(int id) throws SQLException {
        return customerDAO.deleteCustomer(id);
    }
}
