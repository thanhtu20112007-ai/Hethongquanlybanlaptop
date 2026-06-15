package cuoiky;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LichSuGiaoDichFrame extends JFrame {
    private JTable tblDonHang;
    private DefaultTableModel model;
    private JLabel lblHinhAnh, lblThongTinSP;
    private JButton btnTrangThai, btnHuyXoa;

    private int maDonDangChon = -1;
    private boolean isAdminView;
    private String taiKhoan = "";

    public LichSuGiaoDichFrame(boolean isAdminView, String taiKhoan) {
        this.isAdminView = isAdminView;
        this.taiKhoan = taiKhoan;

        setTitle(isAdminView ? "Admin - Quản Lý Lịch Sử Giao Dịch Toàn Hệ Thống" : "Lịch Sử Mua Hàng Của Bạn");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] cols = {"Chọn", "Mã Đơn", "Tài Khoản", "Người Nhận", "Tên Laptop", "Giá Mua", "SL", "Trạng Thái", "Lý Do Hủy"};
        model = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 0 ? Boolean.class : Object.class; }
            @Override public boolean isCellEditable(int row, int column) { return column == 0; }
        };
        tblDonHang = new JTable(model);
        tblDonHang.setRowHeight(25);

        tblDonHang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblDonHang.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblDonHang.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblDonHang.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblDonHang.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblDonHang.getColumnModel().getColumn(4).setPreferredWidth(150);
        tblDonHang.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblDonHang.getColumnModel().getColumn(6).setPreferredWidth(40);
        tblDonHang.getColumnModel().getColumn(7).setPreferredWidth(130);
        tblDonHang.getColumnModel().getColumn(8).setPreferredWidth(200);

        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.add(new JScrollPane(tblDonHang), BorderLayout.CENTER);

        JPanel pnlBottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlBottomLeft.setBackground(Color.WHITE);
        JButton btnXoaNhieu = new JButton("XÓA CÁC ĐƠN ĐÃ TÍCH CHỌN"); btnXoaNhieu.setBackground(new Color(255, 153, 0)); btnXoaNhieu.setForeground(Color.WHITE); btnXoaNhieu.setFont(new Font("Arial", Font.BOLD, 12)); btnXoaNhieu.addActionListener(e -> xuLyXoaNhieuDon(false));
        JButton btnXoaTatCa = new JButton("XÓA TOÀN BỘ LỊCH SỬ"); btnXoaTatCa.setBackground(new Color(220, 53, 69)); btnXoaTatCa.setForeground(Color.WHITE); btnXoaTatCa.setFont(new Font("Arial", Font.BOLD, 12)); btnXoaTatCa.addActionListener(e -> xuLyXoaNhieuDon(true));
        pnlBottomLeft.add(btnXoaNhieu); pnlBottomLeft.add(btnXoaTatCa);
        pnlLeft.add(pnlBottomLeft, BorderLayout.SOUTH);
        add(pnlLeft, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel(); pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS)); pnlRight.setPreferredSize(new Dimension(350, 600)); pnlRight.setBorder(BorderFactory.createTitledBorder("Chi tiết Laptop trong đơn")); pnlRight.setBackground(Color.WHITE);

        lblHinhAnh = new JLabel("[Chọn đơn hàng để xem ảnh]", SwingConstants.CENTER); lblHinhAnh.setAlignmentX(Component.CENTER_ALIGNMENT); lblHinhAnh.setPreferredSize(new Dimension(250, 160)); lblHinhAnh.setMaximumSize(new Dimension(250, 160));
        lblThongTinSP = new JLabel("<html><div style='padding:10px; font-size:12px;'>Chưa chọn giao dịch nào.</div></html>"); lblThongTinSP.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnTrangThai = new JButton("XÁC NHẬN GIAO HÀNG"); btnTrangThai.setFont(new Font("Arial", Font.BOLD, 13)); btnTrangThai.setForeground(Color.WHITE); btnTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT); btnTrangThai.setVisible(false);
        btnHuyXoa = new JButton("CHỌN 1 ĐƠN HÀNG"); btnHuyXoa.setBackground(new Color(204, 0, 0)); btnHuyXoa.setForeground(Color.WHITE); btnHuyXoa.setFont(new Font("Arial", Font.BOLD, 13)); btnHuyXoa.setAlignmentX(Component.CENTER_ALIGNMENT); btnHuyXoa.setEnabled(false);

        JPanel pnlActions = new JPanel(); pnlActions.setLayout(new BoxLayout(pnlActions, BoxLayout.Y_AXIS)); pnlActions.setBackground(Color.WHITE); pnlActions.add(btnTrangThai); pnlActions.add(Box.createVerticalStrut(10)); pnlActions.add(btnHuyXoa);

        pnlRight.add(Box.createVerticalStrut(15)); pnlRight.add(lblHinhAnh); pnlRight.add(Box.createVerticalStrut(15)); pnlRight.add(lblThongTinSP); pnlRight.add(Box.createVerticalGlue()); pnlRight.add(pnlActions); pnlRight.add(Box.createVerticalStrut(20));
        add(pnlRight, BorderLayout.EAST);

        tblDonHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblDonHang.getSelectedRow();
                if (row >= 0) {
                    maDonDangChon = Integer.parseInt(model.getValueAt(row, 1).toString());
                    String trangThai = model.getValueAt(row, 7).toString();
                    hienThiAnhVaThongSo(maDonDangChon);

                    if (isAdminView) {
                        if (trangThai.equals("Chờ xác nhận")) {
                            btnTrangThai.setVisible(true); btnTrangThai.setText("XÁC NHẬN GIAO HÀNG"); btnTrangThai.setBackground(new Color(0, 153, 51));
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("HỦY ĐƠN CỦA KHÁCH"); btnHuyXoa.setBackground(new Color(255, 102, 0)); btnHuyXoa.setEnabled(true);
                        } else if (trangThai.equals("Đang giao hàng")) {
                            btnTrangThai.setVisible(true); btnTrangThai.setText("XÁC NHẬN HOÀN THÀNH"); btnTrangThai.setBackground(new Color(40, 120, 255));
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("HỦY ĐƠN CỦA KHÁCH"); btnHuyXoa.setBackground(new Color(255, 102, 0)); btnHuyXoa.setEnabled(true);
                        } else if (trangThai.equals("Yêu cầu hoàn tiền")) {
                            btnTrangThai.setVisible(true); btnTrangThai.setText("CHẤP NHẬN HOÀN TIỀN"); btnTrangThai.setBackground(new Color(0, 153, 51));
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("TỪ CHỐI HOÀN TIỀN"); btnHuyXoa.setBackground(new Color(204, 0, 0)); btnHuyXoa.setEnabled(true);
                        } else {
                            btnTrangThai.setVisible(false);
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("XÓA SẠCH DATA ĐƠN NÀY"); btnHuyXoa.setBackground(new Color(204, 0, 0)); btnHuyXoa.setEnabled(true);
                        }
                    } else {
                        if (trangThai.equals("Chờ xác nhận")) {
                            btnTrangThai.setVisible(false);
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("HỦY ĐƠN HÀNG NÀY"); btnHuyXoa.setBackground(new Color(255, 102, 0)); btnHuyXoa.setEnabled(true);
                        } else if (trangThai.equals("Đang giao hàng")) {
                            btnTrangThai.setVisible(true); btnTrangThai.setText("ĐÃ NHẬN ĐƯỢC HÀNG"); btnTrangThai.setBackground(new Color(0, 153, 51));
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("ĐANG GIAO (KHÔNG THỂ HỦY)"); btnHuyXoa.setBackground(Color.GRAY); btnHuyXoa.setEnabled(false);
                        } else if (trangThai.equals("Hoàn thành") || trangThai.equals("Đã nhận được hàng")) {
                            btnTrangThai.setVisible(false);
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("YÊU CẦU HOÀN TIỀN"); btnHuyXoa.setBackground(new Color(255, 153, 0)); btnHuyXoa.setEnabled(true);
                        } else {
                            btnTrangThai.setVisible(false);
                            btnHuyXoa.setVisible(true); btnHuyXoa.setText("XÓA LỊCH SỬ NÀY"); btnHuyXoa.setBackground(new Color(204, 0, 0)); btnHuyXoa.setEnabled(true);
                        }
                    }
                }
            }
        });

        btnTrangThai.addActionListener(e -> {
            if (maDonDangChon == -1) return;
            String text = btnTrangThai.getText();
            if (text.equals("XÁC NHẬN GIAO HÀNG")) updateStatus(maDonDangChon, "Đang giao hàng", null, null, null);
            else if (text.equals("XÁC NHẬN HOÀN THÀNH") || text.equals("ĐÃ NHẬN ĐƯỢC HÀNG")) updateStatus(maDonDangChon, "Đã nhận được hàng", null, null, null);
            else if (text.equals("CHẤP NHẬN HOÀN TIỀN")) updateStatus(maDonDangChon, "Đã hoàn tiền", null, null, "Quản trị viên đã chấp nhận hoàn tiền.");
        });

        btnHuyXoa.addActionListener(e -> {
            if (maDonDangChon == -1) return;
            String btnText = btnHuyXoa.getText();

            if (btnText.equals("HỦY ĐƠN CỦA KHÁCH")) {
                HuyDonDialog dlg = new HuyDonDialog(this, 1);  dlg.setVisible(true);
                if (dlg.isConfirmed()) updateStatus(maDonDangChon, "Đã hủy bởi Admin", dlg.getLyDo(), null, null);
            } else if (btnText.equals("HỦY ĐƠN HÀNG NÀY")) {
                HuyDonDialog dlg = new HuyDonDialog(this, 0); dlg.setVisible(true);
                if (dlg.isConfirmed()) updateStatus(maDonDangChon, "Đã hủy bởi Khách", dlg.getLyDo(), null, null);
            } else if (btnText.equals("YÊU CẦU HOÀN TIỀN")) {
                HuyDonDialog dlg = new HuyDonDialog(this, 2); dlg.setVisible(true);
                if (dlg.isConfirmed()) updateStatus(maDonDangChon, "Yêu cầu hoàn tiền", null, dlg.getLyDo(), null);
            } else if (btnText.equals("TỪ CHỐI HOÀN TIỀN")) {
                String msg = JOptionPane.showInputDialog(this, "Nhập lý do TỪ CHỐI hoàn tiền gửi cho Khách hàng:", "Từ chối", JOptionPane.WARNING_MESSAGE);
                if (msg != null && !msg.trim().isEmpty()) {
                    updateStatus(maDonDangChon, "Từ chối hoàn tiền", null, null, msg);
                }
            } else if (btnText.contains("XÓA")) {
                int confirm = JOptionPane.showConfirmDialog(this, "XÓA VĨNH VIỄN đơn này khỏi lịch sử?", "Xác nhận Xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) deleteOrder(maDonDangChon);
            }
        });

        loadDonHangTuSQL();
    }

    private void xuLyXoaNhieuDon(boolean isXoaTatCa) {
        if (model.getRowCount() == 0) return;
        if (isXoaTatCa) {
            if (JOptionPane.showConfirmDialog(this, "Cảnh báo: Bạn muốn xóa TOÀN BỘ LỊCH SỬ?\nHành động này không thể hoàn tác!", "Xóa tất cả", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = isAdminView ? "DELETE FROM DonHang" : "DELETE FROM DonHang WHERE TaiKhoan = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    if (!isAdminView) ps.setString(1, taiKhoan); ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Đã xóa toàn bộ lịch sử thành công!"); loadDonHangTuSQL(); resetRightPanel();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            return;
        }
        boolean hasSelected = false;
        for (int i = 0; i < model.getRowCount(); i++) { if (Boolean.TRUE.equals(model.getValueAt(i, 0))) { hasSelected = true; break; } }
        if (!hasSelected) { JOptionPane.showMessageDialog(this, "Vui lòng tích chọn Checkbox trước các đơn hàng cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }

        if (JOptionPane.showConfirmDialog(this, "Xóa toàn bộ các đơn đã tích chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM DonHang WHERE MaDon = ?");
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                        ps.setInt(1, Integer.parseInt(model.getValueAt(i, 1).toString())); ps.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Đã xóa hàng loạt thành công!"); loadDonHangTuSQL(); resetRightPanel();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    // --- LOGIC HOÀN TRẢ SỐ LƯỢNG VÀO KHO ---
    private void updateStatus(int maDon, String trangThai, String lyDoHuy, String lyDoHoan, String tinNhanAdmin) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Nếu đơn hàng bị Hủy hoặc được Chấp nhận hoàn tiền -> Trả lại máy tính vào kho SanPham
            if (trangThai.equals("Đã hoàn tiền") || trangThai.equals("Đã hủy bởi Admin") || trangThai.equals("Đã hủy bởi Khách")) {
                String getOrderSql = "SELECT MaSP, SoLuong FROM DonHang WHERE MaDon = ?";
                try(PreparedStatement psGet = conn.prepareStatement(getOrderSql)) {
                    psGet.setInt(1, maDon);
                    ResultSet rs = psGet.executeQuery();
                    if (rs.next()) {
                        String maSP = rs.getString("MaSP");
                        int soLuong = rs.getInt("SoLuong");

                        String updateKhoSql = "UPDATE SanPham SET SoLuongTonKho = SoLuongTonKho + ? WHERE MaSP = ?";
                        try(PreparedStatement psUpdateKho = conn.prepareStatement(updateKhoSql)) {
                            psUpdateKho.setInt(1, soLuong);
                            psUpdateKho.setString(2, maSP);
                            psUpdateKho.executeUpdate();
                        }
                    }
                }
            }

            // Cập nhật trạng thái mới cho Đơn Hàng
            String sql = "UPDATE DonHang SET TrangThai = ?";
            if (lyDoHuy != null) sql += ", LyDoHuy = ?";
            if (lyDoHoan != null) sql += ", LyDoHoanTien = ?";
            if (tinNhanAdmin != null) sql += ", TinNhanAdmin = ?";
            sql += " WHERE MaDon = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            ps.setString(idx++, trangThai);
            if (lyDoHuy != null) ps.setString(idx++, lyDoHuy);
            if (lyDoHoan != null) ps.setString(idx++, lyDoHoan);
            if (tinNhanAdmin != null) ps.setString(idx++, tinNhanAdmin);
            ps.setInt(idx, maDon);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thao tác cập nhật hệ thống thành công!");
            loadDonHangTuSQL(); resetRightPanel();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteOrder(int maDon) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM DonHang WHERE MaDon = ?"); ps.setInt(1, maDon); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Đã xóa!"); loadDonHangTuSQL(); resetRightPanel();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void resetRightPanel() {
        btnTrangThai.setVisible(false);
        btnHuyXoa.setEnabled(false); btnHuyXoa.setText("CHỌN 1 ĐƠN HÀNG"); btnHuyXoa.setBackground(new Color(204, 0, 0));
        lblThongTinSP.setText("<html><div style='padding:10px; font-size:12px;'>Chưa chọn giao dịch nào.</div></html>");
        lblHinhAnh.setIcon(null); lblHinhAnh.setText("[Chọn đơn hàng để xem ảnh]");
        maDonDangChon = -1;
    }

    private void loadDonHangTuSQL() {
        model.setRowCount(0);
        String sql = isAdminView ? "SELECT * FROM DonHang ORDER BY MaDon DESC"
                : "SELECT * FROM DonHang WHERE TaiKhoan = ? ORDER BY MaDon DESC";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!isAdminView) ps.setString(1, taiKhoan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        false,
                        rs.getInt("MaDon"), rs.getString("TaiKhoan"), rs.getString("TenKhachHang"), rs.getString("TenSP"),
                        rs.getDouble("GiaMua"), rs.getInt("SoLuong"), rs.getString("TrangThai"), rs.getString("LyDoHuy")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void hienThiAnhVaThongSo(int maDon) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM DonHang WHERE MaDon = ?");
            ps.setInt(1, maDon); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String imgPath = rs.getString("HinhAnh"); String tThai = rs.getString("TrangThai");
                String ldHuy = rs.getString("LyDoHuy"); if(ldHuy == null) ldHuy = "Không có";
                String ldHoan = rs.getString("LyDoHoanTien");
                String tinNhan = rs.getString("TinNhanAdmin");

                String extraInfo = "";
                if (ldHoan != null && !ldHoan.trim().isEmpty()) {
                    extraInfo += "<b>Y/c Hoàn tiền:</b> <span style='color:#ff6600;'>" + ldHoan + "</span><br>";
                }
                if (tinNhan != null && !tinNhan.trim().isEmpty()) {
                    extraInfo += "<b>Admin nhắn:</b> <span style='color:red;'>" + tinNhan + "</span><br>";
                }

                String infoHtml = "<html><div style='width:250px; font-size:12px; color:#333;'>"
                        + "<b>Mã Đơn:</b> #" + rs.getInt("MaDon") + "<br><b>Tên máy:</b> " + rs.getString("TenSP") + "<br>"
                        + "<b>Giá mua:</b> <b style='color:red;'>" + String.format("%,.0f đ", rs.getDouble("GiaMua")) + "</b><br>"
                        + "<b>Trạng thái:</b> <span style='color:blue;'>" + tThai + "</span><br><b>Lý do hủy:</b> <span style='color:red;'>" + ldHuy + "</span><br>"
                        + extraInfo + "<br>"
                        + "<b>Cấu hình chi tiết:</b><br>" + rs.getString("CauHinh") + "</div></html>";
                lblThongTinSP.setText(infoHtml);

                ImageIcon rawIcon = null;
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    String p = imgPath.trim();
                    if (p.startsWith("http")) {
                        try { java.net.URL url = new java.net.URL(p); java.net.HttpURLConnection connImg = (java.net.HttpURLConnection) url.openConnection(); connImg.setRequestProperty("User-Agent", "Mozilla/5.0"); java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(connImg.getInputStream()); if (bimg != null) rawIcon = new ImageIcon(bimg); } catch (Exception e) {}
                    } else { if (new File(p).exists()) rawIcon = new ImageIcon(p); }
                }
                if (rawIcon != null) { lblHinhAnh.setText(""); lblHinhAnh.setIcon(new ImageIcon(rawIcon.getImage().getScaledInstance(250, 160, Image.SCALE_SMOOTH))); }
                else { lblHinhAnh.setIcon(null); lblHinhAnh.setText("[Không tìm thấy file ảnh]"); }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}