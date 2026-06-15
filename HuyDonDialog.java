package cuoiky;

import javax.swing.*;
import java.awt.*;

public class HuyDonDialog extends JDialog {
    private String lyDoDuocChon = "";
    private boolean isConfirmed = false;

    // mode: 0 = Khách Hủy, 1 = Admin Hủy, 2 = Yêu Cầu Hoàn Tiền
    public HuyDonDialog(JFrame parent, int mode) {
        super(parent, mode == 2 ? "Yêu cầu hoàn tiền" : "Lý do hủy đơn hàng", true);
        setSize(450, 280);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlMain = new JPanel(new GridLayout(3, 1, 10, 10));
        pnlMain.setOpaque(false);
        pnlMain.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        pnlMain.add(new JLabel("<html><b>" + (mode == 2 ? "Vui lòng chọn lý do muốn hoàn tiền:" : "Vui lòng cho biết lý do hủy đơn:") + "</b></html>"));

        String[] lyDos;
        if (mode == 1) { // Admin hủy
            lyDos = new String[]{"Hết hàng tồn kho", "Khách hàng không phản hồi", "Sai giá sản phẩm", "Lỗi kỹ thuật hệ thống", "Đơn hàng gian lận/ảo", "Khách yêu cầu hủy qua điện thoại", "Khác (Nhập chi tiết bên dưới)"};
        } else if (mode == 2) { // Khách hoàn tiền
            lyDos = new String[]{"Sản phẩm bị lỗi kỹ thuật / Không lên nguồn", "Giao sai cấu hình máy", "Máy bị trầy xước / Móp méo do vận chuyển", "Máy hoạt động không ổn định / Quá nóng", "Sản phẩm không giống mô tả", "Sạc hoặc Pin bị lỗi", "Màn hình bị điểm chết / Sọc", "Giao thiếu phụ kiện kèm theo", "Thay đổi ý định (Chấp nhận mất phí đổi trả)", "Lý do khác (Nhập chi tiết bên dưới)"};
        } else { // Khách hủy
            lyDos = new String[]{"Thay đổi ý định không muốn mua nữa", "Tìm thấy nơi khác bán giá rẻ hơn", "Đặt trùng đơn / Đặt nhầm", "Thời gian giao hàng quá lâu", "Muốn thay đổi cấu hình/máy khác", "Lý do khác (Nhập chi tiết bên dưới)"};
        }

        JComboBox<String> cbxLyDo = new JComboBox<>(lyDos);
        pnlMain.add(cbxLyDo);

        JTextField txtKhac = new JTextField("Nhập lý do chi tiết...");
        txtKhac.setForeground(Color.GRAY); txtKhac.setEnabled(false);
        pnlMain.add(txtKhac);

        cbxLyDo.addActionListener(e -> {
            if (cbxLyDo.getSelectedIndex() == lyDos.length - 1) {
                txtKhac.setEnabled(true); txtKhac.setText(""); txtKhac.setForeground(Color.BLACK);
            } else {
                txtKhac.setEnabled(false); txtKhac.setText("Nhập lý do chi tiết..."); txtKhac.setForeground(Color.GRAY);
            }
        });

        JButton btnXacNhan = new JButton(mode == 2 ? "GỬI YÊU CẦU HOÀN TIỀN" : "XÁC NHẬN HỦY ĐƠN");
        btnXacNhan.setBackground(new Color(204, 0, 0)); btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.addActionListener(e -> {
            if (cbxLyDo.getSelectedIndex() == lyDos.length - 1) {
                lyDoDuocChon = txtKhac.getText().trim();
                if (lyDoDuocChon.isEmpty() || lyDoDuocChon.equals("Nhập lý do chi tiết...")) return;
            } else {
                lyDoDuocChon = cbxLyDo.getSelectedItem().toString();
            }
            isConfirmed = true; this.dispose();
        });

        add(pnlMain, BorderLayout.CENTER);
        add(btnXacNhan, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return isConfirmed; }
    public String getLyDo() { return lyDoDuocChon; }
}