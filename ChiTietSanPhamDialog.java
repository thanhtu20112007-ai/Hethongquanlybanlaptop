package cuoiky;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ChiTietSanPhamDialog extends JDialog {

    // Đã thêm tham số int tonKho vào hàm khởi tạo
    public ChiTietSanPhamDialog(TrangChu parent, String ten, double giaGoc, double giaMoi,
                                String hinhAnhPath, String cpu, String ram, String ssd, String vga, String manHinh, int tonKho) {
        super(parent, "Chi tiết sản phẩm: " + ten, true);
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlMain = new JPanel(new GridLayout(1, 2, 20, 20));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHinhAnhTo = new JLabel("", SwingConstants.CENTER);
        ImageIcon rawIcon = null;

        if (hinhAnhPath != null && !hinhAnhPath.trim().isEmpty()) {
            String pathTrim = hinhAnhPath.trim();
            if (pathTrim.startsWith("http://") || pathTrim.startsWith("https://")) {
                try {
                    rawIcon = new ImageIcon(new java.net.URL(pathTrim));
                } catch (Exception e) {}
            } else {
                File file = new File(pathTrim);
                if (file.exists()) {
                    rawIcon = new ImageIcon(pathTrim);
                }
            }
        }

        if (rawIcon != null && rawIcon.getImage() != null) {
            lblHinhAnhTo.setIcon(new ImageIcon(rawIcon.getImage().getScaledInstance(400, 350, Image.SCALE_SMOOTH)));
        } else {
            lblHinhAnhTo.setText("<html><h2 style='color:red;'>[Lỗi hiển thị ảnh sản phẩm]</h2></html>");
        }
        pnlMain.add(lblHinhAnhTo);

        JPanel pnlChiTiet = new JPanel(); pnlChiTiet.setLayout(new BoxLayout(pnlChiTiet, BoxLayout.Y_AXIS)); pnlChiTiet.setBackground(Color.WHITE);
        pnlChiTiet.add(new JLabel("<html><h2 style='font-size:24px; color:#333333; margin-bottom:5px;'>" + ten + "</h2></html>"));
        pnlChiTiet.add(Box.createVerticalStrut(5));
        pnlChiTiet.add(new JLabel("<html><b style='color:#ff6600; font-size:28px;'>" + String.format("%,.0f đ", giaMoi) + "</b> <strike style='color:gray; font-size:18px;'>" + String.format("%,.0f đ", giaGoc) + "</strike></html>"));
        pnlChiTiet.add(Box.createVerticalStrut(10));

        pnlChiTiet.add(new JLabel("<html><div style='background-color:#F8F9FA; padding:15px; border-radius:10px; width:400px;'><h3 style='margin:top:0px; color:#ff6600;'>Cấu hình:</h3><b>CPU:</b> " + cpu + "<br><b>RAM:</b> " + ram + "<br><b>SSD:</b> " + ssd + "<br><b>VGA:</b> " + vga + "<br><b>Màn hình:</b> " + manHinh + "</div></html>"));
        pnlChiTiet.add(Box.createVerticalStrut(15));

        JPanel pnlSoLuong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pnlSoLuong.setBackground(Color.WHITE);
        pnlSoLuong.add(new JLabel("Số lượng:   "));
        JButton btnTru = new JButton("-"); btnTru.setBackground(Color.WHITE);
        JTextField txtSoLuong = new JTextField("1", 3); txtSoLuong.setHorizontalAlignment(JTextField.CENTER); txtSoLuong.setEditable(false);
        JButton btnCong = new JButton("+"); btnCong.setBackground(Color.WHITE);

        btnTru.addActionListener(e -> {
            int sl = Integer.parseInt(txtSoLuong.getText());
            if(sl > 1) txtSoLuong.setText(String.valueOf(sl - 1));
        });

        // KIỂM TRA TỒN KHO KHI BẤM NÚT CỘNG
        btnCong.addActionListener(e -> {
            int sl = Integer.parseInt(txtSoLuong.getText());
            if (sl < tonKho) {
                txtSoLuong.setText(String.valueOf(sl + 1));
            } else {
                JOptionPane.showMessageDialog(this, "Kho chỉ còn " + tonKho + " máy. Không thể thêm số lượng!", "Giới hạn tồn kho", JOptionPane.WARNING_MESSAGE);
            }
        });

        pnlSoLuong.add(btnTru); pnlSoLuong.add(txtSoLuong); pnlSoLuong.add(btnCong);
        pnlChiTiet.add(pnlSoLuong); pnlChiTiet.add(Box.createVerticalStrut(15));

        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 10, 0)); pnlButtons.setBackground(Color.WHITE); pnlButtons.setMaximumSize(new Dimension(400, 50));
        String cauHinhNgan = cpu + ", " + ram + ", " + ssd;

        JButton btnMuaNgay = new JButton("<html><center><b style='font-size:14px;'>MUA NGAY</b><br><span style='font-size:10px;'>(Voucher giảm 200k)</span></center></html>");
        btnMuaNgay.setBackground(new Color(255, 102, 0)); btnMuaNgay.setForeground(Color.WHITE);

        JButton btnThemGio = new JButton("<html><center><b style='font-size:14px;'>THÊM VÀO GIỎ HÀNG</b><br><span style='font-size:10px;'>Mua thêm sản phẩm khác</span></center></html>");
        btnThemGio.setBackground(new Color(40, 120, 255)); btnThemGio.setForeground(Color.WHITE);

        // NẾU HẾT HÀNG -> KHÓA NÚT MUA
        if (tonKho <= 0) {
            btnMuaNgay.setEnabled(false);
            btnThemGio.setEnabled(false);
            btnMuaNgay.setText("HẾT HÀNG");
            btnMuaNgay.setBackground(Color.GRAY);
            btnThemGio.setBackground(Color.GRAY);
        } else {
            btnMuaNgay.addActionListener(e -> {
                java.util.List<GioHangItem> listMuaNhanh = new java.util.ArrayList<>();
                listMuaNhanh.add(new GioHangItem(ten, giaGoc, giaMoi, hinhAnhPath, cauHinhNgan, Integer.parseInt(txtSoLuong.getText())));
                this.dispose(); new ThanhToanDialog(parent, listMuaNhanh).setVisible(true);
            });

            btnThemGio.addActionListener(e -> {
                int sl = Integer.parseInt(txtSoLuong.getText());
                parent.themVaoGioHang(new GioHangItem(ten, giaGoc, giaMoi, hinhAnhPath, cauHinhNgan, sl));
                JOptionPane.showMessageDialog(this, "Đã thêm " + sl + " máy vào giỏ hàng!"); this.dispose();
            });
        }

        pnlButtons.add(btnMuaNgay); pnlButtons.add(btnThemGio);
        pnlChiTiet.add(pnlButtons); pnlMain.add(pnlChiTiet); add(pnlMain, BorderLayout.CENTER);
    }
}