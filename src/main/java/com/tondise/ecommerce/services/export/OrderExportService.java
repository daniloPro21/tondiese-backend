package com.tondise.ecommerce.services.export;

import com.tondise.ecommerce.dao.models.Order;
import com.tondise.ecommerce.dao.repository.OrderRepository;
import com.tondise.ecommerce.dao.utils.ExcelReportGenerator;
import com.tondise.ecommerce.dao.utils.PdfReportGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderExportService {

    private static final List<String> HEADERS = List.of(
            "N° commande", "Client", "Statut", "Paiement", "Sous-total", "Remise", "Total", "Date");

    private final OrderRepository orderRepository;
    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;

    public byte[] exportPdf() {
        return pdfReportGenerator.generate("Commandes Tondise", HEADERS, rows());
    }

    public byte[] exportExcel() {
        return excelReportGenerator.generate("Commandes", HEADERS, rows());
    }

    private List<List<String>> rows() {
        return orderRepository.findAll().stream().map(this::toRow).toList();
    }

    private List<String> toRow(Order order) {
        return List.of(
                order.getOrderNumber(),
                order.getUser().getFullName(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getSubtotal().toPlainString(),
                order.getDiscount().toPlainString(),
                order.getTotal().toPlainString(),
                String.valueOf(order.getCreated()));
    }
}
