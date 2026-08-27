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
            c.setPhone("-");
        }
        if (c.getCreatedAt() == null || c.getCreatedAt().trim().isEmpty()) {
            c.setCreatedAt(DateUtil.today());
        }
        return customerDAO.insertCustomer(c);
    }

    public Customer findOrCreateCustomer(String name, String phone, String brand, String model, String regNo) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            name = "Walk-in Customer";
        }
        name = name.trim();
        phone = phone != null ? phone.trim() : "";

        Customer existing = null;
        if (!phone.isEmpty() && !"-".equals(phone)) {
            existing = customerDAO.findCustomerByPhone(phone);
        }
        if (existing == null && !name.equalsIgnoreCase("Walk-in Customer")) {
            existing = customerDAO.findCustomerByName(name);
        }

        if (existing != null) {
            boolean changed = false;
            if ((existing.getPhone() == null || existing.getPhone().equals("-") || existing.getPhone().isEmpty()) && !phone.isEmpty()) {
                existing.setPhone(phone);
                changed = true;
            }
            if ((existing.getVehicleBrand() == null || existing.getVehicleBrand().isEmpty()) && brand != null && !brand.trim().isEmpty()) {
                existing.setVehicleBrand(brand.trim());
                changed = true;
            }
            if ((existing.getVehicleModel() == null || existing.getVehicleModel().isEmpty()) && model != null && !model.trim().isEmpty()) {
                existing.setVehicleModel(model.trim());
                changed = true;
            }
            if ((existing.getVehicleNumber() == null || existing.getVehicleNumber().isEmpty()) && regNo != null && !regNo.trim().isEmpty()) {
                existing.setVehicleNumber(regNo.trim());
                changed = true;
            }
            if (changed) {
                customerDAO.updateCustomer(existing);
            }
            return existing;
        }

        Customer c = new Customer();
        c.setName(name);
        c.setPhone(!phone.isEmpty() ? phone : "-");
        c.setVehicleBrand(brand != null ? brand.trim() : "");
        c.setVehicleModel(model != null ? model.trim() : "");
        c.setVehicleNumber(regNo != null ? regNo.trim() : "");
        c.setNotes("Recorded from Job Card on " + DateUtil.today());
        c.setCreatedAt(DateUtil.today());
        customerDAO.insertCustomer(c);
        return c;
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
