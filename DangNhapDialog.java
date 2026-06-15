package cuoiky;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DangNhapDialog extends JDialog {
    // Component Đăng Nhập
    private JTextField txtLoginEmail;
    private JPasswordField txtLoginPassword;

    // Component Đăng Ký
    private JTextField txtRegHoTen;
    private JTextField txtRegSdt;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPassword;

    private TrangChu trangChuParent;

    public DangNhapDialog(TrangChu parent) {
        super(parent, "Xác thực tài khoản", true);
        this.trangChuParent = parent;

        setSize(420, 380);
        setLocationRelativeTo(parent);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // ============================================
        // 1. TAB ĐĂNG NHẬP (Nút Đăng Nhập trải dài)
        // ============================================
        JPanel pnlLogin = new JPanel();
        pnlLogin.setLayout(new BoxLayout(pnlLogin, BoxLayout.Y_AXIS));
        pnlLogin.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        pnlLogin.setBackground(Color.WHITE);

        JPanel pnlLogEmail = new JPanel(new BorderLayout(0, 5)); pnlLogEmail.setOpaque(false);
        pnlLogEmail.setMaximumSize(new Dimension(360, 50));
        pnlLogEmail.add(new JLabel("<html><b>Email:</b></html>"), BorderLayout.NORTH);
        txtLoginEmail = new JTextField(); pnlLogEmail.add(txtLoginEmail, BorderLayout.CENTER);

        JPanel pnlLogPass = new JPanel(new BorderLayout(0, 5)); pnlLogPass.setOpaque(false);
        pnlLogPass.setMaximumSize(new Dimension(360, 50));
        pnlLogPass.add(new JLabel("<html><b>Mật khẩu:</b></html>"), BorderLayout.NORTH);
        txtLoginPassword = new JPasswordField(); pnlLogPass.add(txtLoginPassword, BorderLayout.CENTER);

        // Nút Đăng nhập giờ chiếm 1 hàng duy nhất
        JPanel pnlLogButtons = new JPanel(new GridLayout(1, 1, 0, 0));
        pnlLogButtons.setOpaque(false);
        pnlLogButtons.setMaximumSize(new Dimension(360, 40));
        pnlLogButtons.setPreferredSize(new Dimension(360, 40));

        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setBackground(new Color(255, 102, 0));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> xuLyDangNhap());

        pnlLogButtons.add(btnLogin);

        pnlLogin.add(pnlLogEmail);
        pnlLogin.add(Box.createVerticalStrut(15));
        pnlLogin.add(pnlLogPass);
        pnlLogin.add(Box.createVerticalStrut(25));
        pnlLogin.add(pnlLogButtons);

        // ============================================
        // 2. TAB ĐĂNG KÝ
        // ============================================
        JPanel pnlRegister = new JPanel();
        pnlRegister.setLayout(new BoxLayout(pnlRegister, BoxLayout.Y_AXIS));
        pnlRegister.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        pnlRegister.setBackground(Color.WHITE);

        JPanel pnlHoTen = new JPanel(new BorderLayout(0, 2)); pnlHoTen.setOpaque(false);
        pnlHoTen.setMaximumSize(new Dimension(360, 42));
        pnlHoTen.add(new JLabel("Họ và tên:"), BorderLayout.NORTH);
        txtRegHoTen = new JTextField(); pnlHoTen.add(txtRegHoTen, BorderLayout.CENTER);

        JPanel pnlSdt = new JPanel(new BorderLayout(0, 2)); pnlSdt.setOpaque(false);
        pnlSdt.setMaximumSize(new Dimension(360, 42));
        pnlSdt.add(new JLabel("Số điện thoại:"), BorderLayout.NORTH);
        txtRegSdt = new JTextField(); pnlSdt.add(txtRegSdt, BorderLayout.CENTER);

        JPanel pnlRegEmail = new JPanel(new BorderLayout(0, 2)); pnlRegEmail.setOpaque(false);
        pnlRegEmail.setMaximumSize(new Dimension(360, 42));
        pnlRegEmail.add(new JLabel("Email:"), BorderLayout.NORTH);
        txtRegEmail = new JTextField(); pnlRegEmail.add(txtRegEmail, BorderLayout.CENTER);

        JPanel pnlRegPass = new JPanel(new BorderLayout(0, 2)); pnlRegPass.setOpaque(false);
        pnlRegPass.setMaximumSize(new Dimension(360, 42));
        pnlRegPass.add(new JLabel("Mật khẩu:"), BorderLayout.NORTH);
        txtRegPassword = new JPasswordField(); pnlRegPass.add(txtRegPassword, BorderLayout.CENTER);

        JPanel pnlRegBtnContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlRegBtnContainer.setOpaque(false);
        pnlRegBtnContainer.setMaximumSize(new Dimension(360, 45));

        JButton btnRegister = new JButton("ĐĂNG KÝ TÀI KHOẢN");
        btnRegister.setPreferredSize(new Dimension(200, 38));
        btnRegister.setBackground(new Color(40, 120, 255));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> xuLyDangKy());
        pnlRegBtnContainer.add(btnRegister);

        pnlRegister.add(pnlHoTen);
        pnlRegister.add(Box.createVerticalStrut(8));
        pnlRegister.add(pnlSdt);
        pnlRegister.add(Box.createVerticalStrut(8));
        pnlRegister.add(pnlRegEmail);
        pnlRegister.add(Box.createVerticalStrut(8));
        pnlRegister.add(pnlRegPass);
        pnlRegister.add(Box.createVerticalStrut(15));
        pnlRegister.add(pnlRegBtnContainer);

        tabbedPane.addTab("Đăng Nhập", pnlLogin);
        tabbedPane.addTab("Đăng Ký", pnlRegister);

        add(tabbedPane);
    }

    private void xuLyDangNhap() {
        String email = txtLoginEmail.getText();
        String pass = new String(txtLoginPassword.getPassword());

        if(email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Email và Mật khẩu!"); return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT MaVaiTro, HoTen, Email FROM NguoiDung WHERE Email = ? AND MatKhau = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email); ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int role = rs.getInt("MaVaiTro");
                String hoTen = rs.getString("HoTen");
                String mail = rs.getString("Email");
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Xin chào " + hoTen);
                this.dispose();
                trangChuParent.capNhatDangNhapThanhCong(hoTen, role, mail);
            } else {
                JOptionPane.showMessageDialog(this, "Sai Email hoặc Mật khẩu!", "Báo lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void xuLyDangKy() {
        String hoTen = txtRegHoTen.getText().trim();
        String sdt = txtRegSdt.getText().trim();
        String email = txtRegEmail.getText().trim();
        String pass = new String(txtRegPassword.getPassword());

        // 1. Kiểm tra rỗng
        if (hoTen.isEmpty() || sdt.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các thông tin đăng ký!", "Báo lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Bắt lỗi Regex Số điện thoại (Phải bắt đầu bằng số 0 và có đúng 10 chữ số)
        if (!sdt.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! (Phải gồm 10 số và bắt đầu bằng số 0)", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Bắt lỗi Regex Email (Phải có @ và dấu chấm tên miền)
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ! (Ví dụ chuẩn: abc@gmail.com)", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO NguoiDung (Email, MatKhau, HoTen, SoDienThoai, MaVaiTro) VALUES (?, ?, ?, ?, 3)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);   ps.setString(2, pass);
            ps.setString(3, hoTen);   ps.setString(4, sdt);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng chuyển sang Tab Đăng Nhập.");
            txtRegHoTen.setText(""); txtRegSdt.setText(""); txtRegEmail.setText(""); txtRegPassword.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Email này đã được sử dụng. Vui lòng chọn Email khác!", "Lỗi Đăng Ký", JOptionPane.ERROR_MESSAGE);
        }
    }
}