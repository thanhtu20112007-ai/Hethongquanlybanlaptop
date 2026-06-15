package cuoiky;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Year;
import java.util.Random;

public class ThongKeFrame extends JFrame {
    private double[] dataValues = new double[31];
    private int dataSize = 12;
    private double maxDoanhThu = 0;

    private JComboBox<Integer> cbxNam;
    private JComboBox<String> cbxThang;
    private ChartPanel chartPanel;

    public ThongKeFrame() {
        setTitle("Admin - Phân Tích Doanh Thu Hệ Thống");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlTop.setBackground(new Color(245, 246, 250));
        pnlTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        pnlTop.add(new JLabel("<html><b>Chọn Năm:</b></html>"));
        cbxNam = new JComboBox<>();
        int currentYear = Year.now().getValue();
        for (int i = currentYear - 2; i <= currentYear + 1; i++) cbxNam.addItem(i);
        cbxNam.setSelectedItem(currentYear);
        pnlTop.add(cbxNam);

        pnlTop.add(new JLabel("<html><b>Chọn Tháng:</b></html>"));
        cbxThang = new JComboBox<>();
        cbxThang.addItem("Cả năm (Hiển thị 12 Tháng)");
        for (int i = 1; i <= 12; i++) cbxThang.addItem("Tháng " + i);
        pnlTop.add(cbxThang);

        JButton btnXem = new JButton("XEM THỐNG KÊ");
        btnXem.setBackground(new Color(0, 102, 204));
        btnXem.setForeground(Color.WHITE);
        btnXem.setFont(new Font("Arial", Font.BOLD, 12));
        btnXem.addActionListener(e -> reloadData());
        pnlTop.add(btnXem);

        JButton btnDemo = new JButton("TẠO DATA DEMO");
        btnDemo.setBackground(new Color(40, 167, 69));
        btnDemo.setForeground(Color.WHITE);
        btnDemo.setFont(new Font("Arial", Font.BOLD, 12));
        btnDemo.addActionListener(e -> taoDuLieuDemoVaoSQL());
        pnlTop.add(btnDemo);

        add(pnlTop, BorderLayout.NORTH);

        chartPanel = new ChartPanel();
        add(chartPanel, BorderLayout.CENTER);

        JLabel lblTitle = new JLabel("BIỂU ĐỘ TỔNG QUAN DOANH THU", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(lblTitle, BorderLayout.NORTH);

        reloadData();
    }

    private void reloadData() {
        int nam = (int) cbxNam.getSelectedItem();
        int thang = cbxThang.getSelectedIndex();

        for (int i = 0; i < 31; i++) dataValues[i] = 0;
        maxDoanhThu = 0;
        dataSize = (thang == 0) ? 12 : 31;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement ps;

            if (thang == 0) {
                sql = "SELECT MONTH(NgayDat) AS CotX, SUM(GiaMua * SoLuong) AS TongTien FROM DonHang " +
                        "WHERE TrangThai IN (N'Hoàn thành', N'Đã nhận được hàng', N'Từ chối hoàn tiền') " +
                        "  AND TrangThai NOT IN (N'Yêu cầu hoàn tiền', N'Đã hoàn tiền', N'Chờ xác nhận', N'Đang giao hàng', N'Đã hủy bởi Khách', N'Đã hủy bởi Admin', N'Đã bị hủy bởi Admin') " +
                        "  AND YEAR(NgayDat) = ? " +
                        "GROUP BY MONTH(NgayDat)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, nam);
            } else {
                sql = "SELECT DAY(NgayDat) AS CotX, SUM(GiaMua * SoLuong) AS TongTien FROM DonHang " +
                        "WHERE TrangThai IN (N'Hoàn thành', N'Đã nhận được hàng', N'Từ chối hoàn tiền') " +
                        "  AND TrangThai NOT IN (N'Yêu cầu hoàn tiền', N'Đã hoàn tiền', N'Chờ xác nhận', N'Đang giao hàng', N'Đã hủy bởi Khách', N'Đã hủy bởi Admin', N'Đã bị hủy bởi Admin') " +
                        "  AND YEAR(NgayDat) = ? AND MONTH(NgayDat) = ? " +
                        "GROUP BY DAY(NgayDat)";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, nam);
                ps.setInt(2, thang);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int cotX = rs.getInt("CotX");
                double tien = rs.getDouble("TongTien");
                if (cotX >= 1 && cotX <= dataSize) {
                    dataValues[cotX - 1] = tien;
                    if (tien > maxDoanhThu) maxDoanhThu = tien;
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        chartPanel.repaint();
    }

    private void taoDuLieuDemoVaoSQL() {
        int nam = (int) cbxNam.getSelectedItem();
        int thangChon = cbxThang.getSelectedIndex();

        int msg = JOptionPane.showConfirmDialog(this, "Hệ thống sẽ tự động tạo các đơn hàng ảo (Đã nhận được hàng) lưu vào SQL\nđể biểu đồ hiển thị đẹp mắt cho thời gian bạn chọn. Bạn có chắc chắn?", "Tạo Data Demo", JOptionPane.YES_NO_OPTION);
        if(msg != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // ĐÃ SỬA: Lấy CPU, RAM, SSD thay vì lấy CauHinh (vì CauHinh không tồn tại trong DB SanPham)
            PreparedStatement psSP = conn.prepareStatement("SELECT TOP 1 MaSP, TenSP, GiaGoc, HinhAnh, CPU, RAM, SSD FROM SanPham");
            ResultSet rsSP = psSP.executeQuery();
            if (!rsSP.next()) {
                JOptionPane.showMessageDialog(this, "Lỗi: Kho không có sản phẩm nào. Hãy thêm ít nhất 1 laptop vào kho trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maSP = rsSP.getString("MaSP");
            String tenSP = rsSP.getString("TenSP");
            double giaGoc = rsSP.getDouble("GiaGoc");

            String hinhAnh = rsSP.getString("HinhAnh");
            String cpu = rsSP.getString("CPU");
            String ram = rsSP.getString("RAM");
            String ssd = rsSP.getString("SSD");

            // Nối chuỗi tạo thành Cấu hình
            String cauHinh = (cpu != null ? cpu : "") + ", " + (ram != null ? ram : "") + ", " + (ssd != null ? ssd : "");

            String sqlInsert = "INSERT INTO DonHang (TaiKhoan, TenKhachHang, MaSP, TenSP, GiaMua, SoLuong, TrangThai, NgayDat, HinhAnh, CauHinh) VALUES (?, ?, ?, ?, ?, ?, N'Đã nhận được hàng', ?, ?, ?)";
            PreparedStatement psInsert = conn.prepareStatement(sqlInsert);

            Random rand = new Random();
            int monthStart = (thangChon == 0) ? 1 : thangChon;
            int monthEnd = (thangChon == 0) ? 12 : thangChon;

            for (int m = monthStart; m <= monthEnd; m++) {
                int soDonTrongThang = rand.nextInt(5) + 3;
                for(int i = 0; i < soDonTrongThang; i++) {
                    int ngay = rand.nextInt(28) + 1;
                    String ngayDat = String.format("%04d-%02d-%02d 14:30:00", nam, m, ngay);

                    psInsert.setString(1, "demo.teacher@vku.edu.vn");
                    psInsert.setString(2, "Khách Demo Hệ Thống");
                    psInsert.setString(3, maSP);
                    psInsert.setString(4, tenSP);
                    psInsert.setDouble(5, giaGoc);
                    psInsert.setInt(6, rand.nextInt(2) + 1);
                    psInsert.setString(7, ngayDat);
                    psInsert.setString(8, hinhAnh == null ? "" : hinhAnh);
                    psInsert.setString(9, cauHinh);
                    psInsert.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Đang cập nhật biểu đồ...");
            reloadData();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hệ thống SQL từ chối tạo dữ liệu. Chi tiết lỗi:\n" + ex.getMessage(), "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth(); int height = getHeight();
            int padding = 60; int labelPadding = 40;

            g2d.setColor(Color.WHITE);
            g2d.fillRect(padding + labelPadding, padding + 20, width - (2 * padding) - labelPadding, height - 2 * padding - labelPadding);
            g2d.setColor(Color.BLACK);

            g2d.drawLine(padding + labelPadding, height - padding, padding + labelPadding, padding + 20);
            g2d.drawLine(padding + labelPadding, height - padding, width - padding + 20, height - padding);

            int availableWidth = width - (2 * padding) - labelPadding;
            int barWidth = (availableWidth / dataSize) - (dataSize == 31 ? 2 : 15);

            for (int i = 0; i < dataSize; i++) {
                int x = padding + labelPadding + (i * (barWidth + (dataSize == 31 ? 2 : 15))) + (dataSize == 31 ? 2 : 10);
                int barHeight = maxDoanhThu > 0 ? (int) ((dataValues[i] / maxDoanhThu) * (height - 2 * padding - labelPadding - 40)) : 0;
                int y = height - padding - barHeight;

                if (dataValues[i] > 0) {
                    GradientPaint gp = new GradientPaint(x, y, new Color(255, 120, 30), x, y + barHeight, new Color(200, 50, 0));
                    g2d.setPaint(gp);
                    g2d.fillRect(x, y, barWidth, barHeight);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(x, y, barWidth, barHeight);
                }

                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, dataSize == 31 ? 10 : 12));
                String labelX = (dataSize == 12) ? "T" + (i + 1) : String.valueOf(i + 1);
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(labelX);
                g2d.drawString(labelX, x + (barWidth - labelWidth) / 2, height - padding + fm.getHeight() + 5);

                if (dataValues[i] > 0) {
                    String moneyStr = String.format("%.1f", dataValues[i] / 1000000) + " Tr";
                    if (dataSize == 12) {
                        g2d.setFont(new Font("Arial", Font.BOLD, 11));
                        int mWidth = g2d.getFontMetrics().stringWidth(moneyStr);
                        g2d.drawString(moneyStr, x + (barWidth - mWidth) / 2, y - 5);
                    } else {
                        g2d.setFont(new Font("Arial", Font.BOLD, 9));
                        AffineTransform orig = g2d.getTransform();
                        g2d.translate(x + barWidth / 2 + 3, y - 5);
                        g2d.rotate(-Math.PI / 2);
                        g2d.drawString(moneyStr, 0, 0);
                        g2d.setTransform(orig);
                    }
                }
            }
        }
    }
}